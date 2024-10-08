package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.SiteStatus;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    @Value("${search.user-agent}")
    private String userAgent;

    @Value("${search.referrer}")
    private String referrer;

    @Value("${search.request-delay}")
    private long requestDelay;

    private boolean indexing = false;
    private ForkJoinPool forkJoinPool = new ForkJoinPool(); // Пул потоков для индексации

    @Override
    public boolean isIndexing() {
        return indexing;
    }

    @Override
    @Transactional
    public synchronized void startIndexing() {
        if (indexing) {
            return;
        }
        indexing = true;
        try {
            List<Site> sites = sitesList.getSites();
            for (Site siteConfig : sites) {
                searchengine.model.Site siteEntity = siteRepository.findByUrl(siteConfig.getUrl());
                if (siteEntity != null) {
                    pageRepository.deleteAllBySite(siteEntity);
                    siteRepository.delete(siteEntity);
                }

                searchengine.model.Site newSite = new searchengine.model.Site();
                newSite.setUrl(siteConfig.getUrl());
                newSite.setName(siteConfig.getName());
                newSite.setStatus(SiteStatus.INDEXING);
                newSite.setStatusTime(LocalDateTime.now());
                siteRepository.save(newSite);

                try {
                    processSitePages(newSite);
                    newSite.setStatus(SiteStatus.INDEXED);
                } catch (Exception e) {
                    newSite.setStatus(SiteStatus.FAILED);
                    newSite.setLastError("Ошибка индексации: " + e.getMessage());
                } finally {
                    newSite.setStatusTime(LocalDateTime.now());
                    siteRepository.save(newSite);
                }
            }
        } finally {
            indexing = false;
        }
    }

    // Метод для обработки страниц сайта
    private void processSitePages(searchengine.model.Site site) throws Exception {
        Set<String> visitedLinks = new HashSet<>();
        forkJoinPool.invoke(new PageCrawler(site, "/", visitedLinks));
    }

    @Override
    public synchronized ResponseEntity<?> stopIndexing() {
        if (!indexing) {
            return ResponseEntity.ok(Map.of("result", false, "error", "Индексация не запущена"));
        }

        // Остановка всех активных потоков
        forkJoinPool.shutdownNow();
        indexing = false;

        // Обновление статуса сайтов в базе данных
        List<searchengine.model.Site> indexingSites = siteRepository.findByStatus(SiteStatus.INDEXING);
        for (searchengine.model.Site site : indexingSites) {
            site.setStatus(SiteStatus.FAILED);
            site.setLastError("Индексация остановлена пользователем");
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }

        return ResponseEntity.ok(Map.of("result", true));
    }

    // Рекурсивная задача для обхода страниц
    private class PageCrawler extends RecursiveTask<Void> {
        private final searchengine.model.Site site;
        private final String path;
        private final Set<String> visitedLinks;

        public PageCrawler(searchengine.model.Site site, String path, Set<String> visitedLinks) {
            this.site = site;
            this.path = path;
            this.visitedLinks = visitedLinks;
        }

        @Override
        protected Void compute() {
            try {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Индексация остановлена пользователем");
                }

                String url = site.getUrl() + path;
                if (visitedLinks.contains(url) || isPageExistsInDatabase(url)) {
                    return null;
                }

                // Получение содержимого страницы с использованием User-Agent и referrer
                Document doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .referrer(referrer)
                        .get();

                visitedLinks.add(url);
                savePage(url, doc);

                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String nextPath = link.attr("href");
                    if (nextPath.startsWith("/")) {
                        nextPath = path + nextPath;
                    }
                    PageCrawler crawler = new PageCrawler(site, nextPath, visitedLinks);
                    crawler.fork();

                    // Задержка между запросами
                    Thread.sleep(requestDelay);
                }
            } catch (InterruptedException e) {
                site.setStatus(SiteStatus.FAILED);
                site.setLastError("Индексация остановлена пользователем: " + e.getMessage());
                site.setStatusTime(LocalDateTime.now());
                siteRepository.save(site);
            } catch (Exception e) {
                site.setStatus(SiteStatus.FAILED);
                site.setLastError("Ошибка обработки страницы: " + e.getMessage());
                site.setStatusTime(LocalDateTime.now());
                siteRepository.save(site);
            }
            return null;
        }

        // Метод для проверки существования страницы в базе данных
        private boolean isPageExistsInDatabase(String url) {
            return pageRepository.findByPath(url) != null;
        }

        // Метод для сохранения страницы в базе данных
        private void savePage(String url, Document doc) {
            Page page = new Page();
            page.setSite(site);
            page.setPath(url);
            page.setCode(200);
            page.setContent(doc.html());
            pageRepository.save(page);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }
    }
}
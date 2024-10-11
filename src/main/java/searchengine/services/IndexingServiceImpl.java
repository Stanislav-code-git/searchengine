package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IndexingServiceImpl implements IndexingService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SitesList sitesList;
    private final int numberOfThreads;
    private ExecutorService executorService;
    private List<Future<?>> indexingTasks; // Список задач индексации
    private AtomicBoolean indexingActive;

    @Autowired
    public IndexingServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesList) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.sitesList = sitesList;
        this.numberOfThreads = Runtime.getRuntime().availableProcessors();
        this.indexingActive = new AtomicBoolean(false);
        this.indexingTasks = new ArrayList<>();  // Инициализируем список задач
    }

    @Override
    public boolean startIndexing() {
        if (indexingActive.get()) {
            return false;  // Если индексация уже активна, то не запускаем её снова
        }

        clearData();
        List<searchengine.config.Site> siteConfigs = sitesList.getSites();

        if (siteConfigs == null || siteConfigs.isEmpty()) {
            throw new IllegalStateException("No sites configured for indexing.");
        }

        executorService = Executors.newFixedThreadPool(numberOfThreads);
        indexingActive.set(true);  // Устанавливаем флаг активности индексации

        for (searchengine.config.Site siteConfig : siteConfigs) {
            Site site = new Site();
            site.setStatus(Status.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            site.setUrl(siteConfig.getUrl());
            site.setName(siteConfig.getName());

            Site savedSite = siteRepository.save(site);

            Future<?> indexingTask = executorService.submit(() -> {
                try {
                    indexSitePages(savedSite);
                    savedSite.setStatus(Status.INDEXED);
                    savedSite.setStatusTime(LocalDateTime.now());
                } catch (IOException e) {
                    String errorMessage = "Failed to index site at " + savedSite.getUrl() + ": " + e.getMessage();
                    savedSite.setStatus(Status.FAILED);
                    savedSite.setLastError(errorMessage);
                    e.printStackTrace();
                } finally {
                    siteRepository.save(savedSite);
                }
            });
            indexingTasks.add(indexingTask);  // Добавляем задачу в список для отслеживания
        }

        executorService.shutdown();
        return true;
    }

    @Override
    public boolean stopIndexing() {
        if (!indexingActive.get()) {
            return false;  // Если индексация не была активной, возвращаем ошибку
        }

        // Останавливаем все активные задачи индексации
        executorService.shutdownNow();  // Принудительная остановка потоков

        // Обновляем статусы всех сайтов и страниц на FAILED
        List<Site> sites = siteRepository.findAll();
        for (Site site : sites) {
            if (site.getStatus() == Status.INDEXING) {
                site.setStatus(Status.FAILED);
                site.setStatusTime(LocalDateTime.now());
                site.setLastError("Индексация остановлена пользователем");
                siteRepository.save(site);
            }

            List<Page> pages = pageRepository.findBySite(site);
            for (Page page : pages) {
                page.setCode(500);  // Устанавливаем код ошибки
                page.setContent("Индексация остановлена пользователем");
                pageRepository.save(page);
            }
        }

        indexingActive.set(false);  // Сбрасываем флаг активности
        return true;
    }

    private void indexSitePages(Site site) throws IOException {
        Set<String> visitedLinks = new HashSet<>();
        crawlPage(site, site.getUrl(), visitedLinks);
    }

    private void crawlPage(Site site, String url, Set<String> visitedLinks) throws IOException {
        if (visitedLinks.contains(url)) {
            return;
        }

        visitedLinks.add(url);
        Document document = Jsoup.connect(url).get();
        String content = document.body().text();
        int statusCode = 200;

        Page page = new Page();
        page.setSite(site);
        page.setPath(url);
        page.setCode(statusCode);
        page.setContent(content);

        pageRepository.save(page);

        Elements links = document.select("a[href]");
        for (Element link : links) {
            String absUrl = link.absUrl("href");

            if (absUrl.startsWith(site.getUrl())) {
                crawlPage(site, absUrl, visitedLinks);
            }
        }
    }

    @Override
    public boolean isIndexing() {
        return indexingActive.get();
    }

    @Override
    public void clearData() {
        pageRepository.deleteAll();
        siteRepository.deleteAll();
    }
}
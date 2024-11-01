package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IndexingServiceImpl implements IndexingService {

    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private SitesList sitesList;
    private int numberOfThreads;
    private ExecutorService executorService;
    private List<Future<?>> indexingTasks;
    private AtomicBoolean indexingActive;

    public IndexingServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesList) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.sitesList = sitesList;
        this.numberOfThreads = Runtime.getRuntime().availableProcessors();
        this.indexingActive = new AtomicBoolean(false);
        this.indexingTasks = new ArrayList<>();
    }

    private boolean isIndexingActive() {
        return indexingActive.get();
    }

    @Override
    public boolean startIndexing() {
        if (isIndexingActive()) {
            return false;
        }

        clearData();

        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(numberOfThreads);
        }

        indexingActive.set(true);
        List<Site> siteConfigs = sitesList.getSites();

        if (siteConfigs == null || siteConfigs.isEmpty()) {
            throw new IllegalStateException("No sites configured for indexing.");
        }

        for (Site siteConfig : siteConfigs) {
            searchengine.model.Site site = new searchengine.model.Site();
            site.setStatus(Status.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            site.setUrl(siteConfig.getUrl());
            site.setName(siteConfig.getName());

            searchengine.model.Site savedSite = siteRepository.save(site);

            Future<?> indexingTask = executorService.submit(() -> {
                try {
                    indexSitePages(savedSite);
                    savedSite.setStatus(Status.INDEXED);
                    savedSite.setStatusTime(LocalDateTime.now());
                } catch (IOException e) {
                    String errorMessage = "Failed to index site at " + savedSite.getUrl() + ": " + e.getMessage();
                    savedSite.setStatus(Status.FAILED);
                    savedSite.setLastError(errorMessage);
                } finally {
                    siteRepository.save(savedSite);
                }
            });
            indexingTasks.add(indexingTask);
        }

        return true;
    }

    @Override
    public boolean stopIndexing() {
        if (!isIndexingActive()) {
            return false;
        }

        executorService.shutdownNow();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        List<searchengine.model.Site> sites = siteRepository.findAll();
        for (searchengine.model.Site site : sites) {
            if (site.getStatus() == Status.INDEXING) {
                site.setStatus(Status.FAILED);
                site.setStatusTime(LocalDateTime.now());
                site.setLastError("Индексация остановлена пользователем");
                siteRepository.save(site);
            }

            List<Page> pages = pageRepository.findBySite(site);
            for (Page page : pages) {
                page.setCode(500);
                page.setContent("Индексация остановлена пользователем");
                pageRepository.save(page);
            }
        }

        indexingActive.set(false);
        return true;
    }

    private void indexSitePages(searchengine.model.Site site) throws IOException {
        Set<String> visitedLinks = new HashSet<>();
        crawlPage(site, site.getUrl(), visitedLinks);
    }

    private Page savePage(searchengine.model.Site site, String url, int statusCode, String content) {
        Page page = new Page();
        page.setSite(site);
        page.setPath(url);
        page.setCode(statusCode);
        page.setContent(content);
        return pageRepository.save(page);
    }

    private void crawlPage(searchengine.model.Site site, String url, Set<String> visitedLinks) throws IOException {
        if (visitedLinks.contains(url) || pageRepository.existsByPath(url)) {
            return;
        }

        visitedLinks.add(url);
        Document document = Jsoup.connect(url).get();
        String content = document.body().text();
        int statusCode = 200;

        savePage(site, url, statusCode, content);

        Elements links = document.select("a[href]");
        for (Element link : links) {
            String absUrl = link.absUrl("href");

            if (absUrl.startsWith(site.getUrl()) && !visitedLinks.contains(absUrl)) {
                crawlPage(site, absUrl, visitedLinks);
            }
        }
    }

    @Override
    public boolean isIndexing() {
        return isIndexingActive();
    }

    @Override
    public void clearData() {
        pageRepository.deleteAll();
        siteRepository.deleteAll();
    }
    @Override
    public boolean indexPage(String url) {
        return true;
    }
    @Override
    public boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            System.out.println("Некорректный URL: " + url);
            return false;
        }
    }
}
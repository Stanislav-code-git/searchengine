package searchengine.services;

import searchengine.model.Site;

public interface SearchEngineService {
    void processUrl(Site site, String url);
}
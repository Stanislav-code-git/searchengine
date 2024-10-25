package searchengine.services;

import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;

public interface PageService {
    Page savePageContent(Site site, String url) throws IOException;
}
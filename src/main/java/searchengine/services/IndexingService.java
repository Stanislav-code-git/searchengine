package searchengine.services;

public interface IndexingService {
    boolean isIndexing();
    boolean startIndexing();
    boolean stopIndexing();
    void clearData();
    boolean isValidUrl(String url);
    boolean indexPage(String url);
}
package searchengine.services;

public interface IndexingService {
    boolean isIndexing();
    boolean startIndexing();
    boolean stopIndexing();
    void clearData();
}
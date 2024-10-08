package searchengine.services;

import org.springframework.http.ResponseEntity;

public interface IndexingService {
    boolean isIndexing();
    void startIndexing();
    ResponseEntity<?> stopIndexing();
}
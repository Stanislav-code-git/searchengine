package searchengine.services;

public interface SearchService {
    SearchResults search(String query, String site, int offset, int limit);
}
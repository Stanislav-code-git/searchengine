package searchengine.services;

import java.util.List;

public class SearchResults {
    private final int totalCount;
    private final List<SearchResultItem> data;

    public SearchResults(int totalCount, List<SearchResultItem> data) {
        this.totalCount = totalCount;
        this.data = data;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<SearchResultItem> getData() {
        return data;
    }
}
package searchengine.services;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SearchResults {
    private final int totalCount;
    private final List<SearchResultItem> data;

    public int getTotalCount() {
        return totalCount;
    }

    public List<SearchResultItem> getData() {
        return data;
    }
}
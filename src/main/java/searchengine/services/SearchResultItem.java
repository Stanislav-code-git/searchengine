package searchengine.services;

import lombok.Data;

@Data
public class SearchResultItem {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;
}
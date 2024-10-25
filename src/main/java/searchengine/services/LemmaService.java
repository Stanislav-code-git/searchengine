package searchengine.services;

import searchengine.model.Page;

import java.util.Map;

public interface LemmaService {
    void processLemmas(Page page, Map<String, Integer> lemmas);
}
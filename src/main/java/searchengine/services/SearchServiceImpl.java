package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private final LemmaAnalyzer lemmaAnalyzer;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;

    public SearchServiceImpl(LemmaAnalyzer lemmaAnalyzer, LemmaRepository lemmaRepository,
                             PageRepository pageRepository, IndexRepository indexRepository) {
        this.lemmaAnalyzer = lemmaAnalyzer;
        this.lemmaRepository = lemmaRepository;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
    }

    @Override
    public SearchResults search(String query, String site, int offset, int limit) {
        Map<String, Integer> lemmaCounts = lemmaAnalyzer.analyzeText(query);

        List<Lemma> lemmas = lemmaRepository.findAllByLemmaIn(lemmaCounts.keySet())
                .stream()
                .sorted(Comparator.comparingInt(Lemma::getFrequency))
                .collect(Collectors.toList());

        List<Page> filteredPages = findPagesByLemmas(lemmas, site);
        List<SearchResultItem> searchResultItems = calculateRelevance(filteredPages, lemmas);

        searchResultItems.sort(Comparator.comparingDouble(SearchResultItem::getRelevance).reversed());

        return new SearchResults(searchResultItems.size(), searchResultItems.subList(offset, Math.min(offset + limit, searchResultItems.size())));
    }

    private List<Page> findPagesByLemmas(List<Lemma> lemmas, String site) {
        List<Page> pages = pageRepository.findPagesByLemma(lemmas.get(0), site);

        for (int i = 1; i < lemmas.size() && !pages.isEmpty(); i++) {
            Lemma lemma = lemmas.get(i);
            List<Page> pagesWithLemma = pageRepository.findPagesByLemma(lemma, site);
            pages.retainAll(pagesWithLemma);
        }

        return pages;
    }

    private List<SearchResultItem> calculateRelevance(List<Page> pages, List<Lemma> lemmas) {
        List<SearchResultItem> resultItems = new ArrayList<>();
        double maxAbsRelevance = 0.0;

        Map<Page, Double> absRelevanceMap = new HashMap<>();

        for (Page page : pages) {
            double absRelevance = calculateAbsRelevance(page, lemmas);
            absRelevanceMap.put(page, absRelevance);
            maxAbsRelevance = Math.max(maxAbsRelevance, absRelevance);
        }

        for (Page page : pages) {
            double absRelevance = absRelevanceMap.get(page);
            double relevance = absRelevance / maxAbsRelevance;

            SearchResultItem item = new SearchResultItem();
            item.setUri(page.getPath());
            item.setTitle(getPageTitle(page.getContent()));
            item.setSnippet(createSnippet(page.getContent(), lemmas));
            item.setRelevance(relevance);

            resultItems.add(item);
        }

        return resultItems;
    }

    private double calculateAbsRelevance(Page page, List<Lemma> lemmas) {
        return lemmas.stream()
                .mapToDouble(lemma -> indexRepository.findRankByPageAndLemma(page, lemma).orElse(0.0))
                .sum();
    }

    private String getPageTitle(String content) {
        return "";
        // Логика для получения заголовка из HTML-контента страницы
    }

    private String createSnippet(String content, List<Lemma> lemmas) {
        return "";
        // Логика для создания сниппета с выделением совпадений жирным
    }
}
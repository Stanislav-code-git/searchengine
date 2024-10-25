package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.Map;

@Service
public class SearchEngineServiceImpl implements SearchEngineService {

    private final PageService pageService;
    private final LemmaAnalyzer lemmaAnalyzer;
    private final LemmaService lemmaService;

    public SearchEngineServiceImpl(PageService pageService, LemmaAnalyzer lemmaAnalyzer, LemmaService lemmaService) {
        this.pageService = pageService;
        this.lemmaAnalyzer = lemmaAnalyzer;
        this.lemmaService = lemmaService;
    }

    @Override
    public void processUrl(Site site, String url) {
        Page page;
        try {
            page = pageService.savePageContent(site, url);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении страницы: " + url);
            return;
        }

        String cleanedText = lemmaAnalyzer.removeHtmlTags(page.getContent());

        Map<String, Integer> lemmas;
        try {
            lemmas = lemmaAnalyzer.analyzeText(cleanedText);
        } catch (Exception e) {
            System.err.println("Ошибка при анализе текста");
            return;
        }

        lemmaService.processLemmas(page, lemmas);
    }
}
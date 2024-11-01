package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LemmaAnalyzer {

    private LuceneMorphology luceneMorphology;

    public LemmaAnalyzer() throws Exception {
        luceneMorphology = new RussianLuceneMorphology();
    }

    public Map<String, Integer> analyzeText(String text) {
        Map<String, Integer> lemmaCounts = new HashMap<>();

        String[] words = text.toLowerCase().split("[^а-яёА-ЯЁ]+");

        for (String word : words) {
            if (word.isEmpty()) continue;

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);

            if (isValidWord(wordBaseForms)) {
                String lemma = luceneMorphology.getNormalForms(word).get(0);
                lemmaCounts.put(lemma, lemmaCounts.getOrDefault(lemma, 0) + 1);
            }
        }
        return lemmaCounts;
    }

    public String removeHtmlTags(String htmlText) {
        return htmlText.replaceAll("<[^>]+>", "");
    }

    private boolean isValidWord(List<String> morphInfos) {
        for (String info : morphInfos) {
            if (info.contains("МЕЖД") || info.contains("ПРЕДЛ") || info.contains("СОЮЗ") || info.contains("ЧАСТ")) {
                return false;
            }
        }
        return true;
    }
}
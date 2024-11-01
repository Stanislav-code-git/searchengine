package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Index;
import searchengine.model.Page;
import searchengine.repository.LemmaRepository;
import searchengine.repository.IndexRepository;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService {

    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    private Lemma saveOrUpdateLemma(String lemmaText, int siteId, int count) {
        Optional<Lemma> optionalLemma = lemmaRepository.findByLemmaAndSiteId(lemmaText, (long) siteId);
        Lemma lemma;
        if (optionalLemma.isEmpty()) {
            lemma = new Lemma();
            lemma.setLemma(lemmaText);
            lemma.setSiteId(siteId);
            lemma.setFrequency(count);
        } else {
            lemma = optionalLemma.get();
            lemma.setFrequency(lemma.getFrequency() + count);
        }
        return lemmaRepository.save(lemma);
    }

    @Transactional
    public void processLemmas(Page page, Map<String, Integer> lemmas) {
        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            Lemma lemma = saveOrUpdateLemma(entry.getKey(), page.getSite().getId(), entry.getValue());

            Index index = new Index();
            index.setPage(page);
            index.setLemma(lemma);
            index.setRank((double) entry.getValue());
            indexRepository.save(index);
        }
    }
}
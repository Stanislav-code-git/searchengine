package searchengine.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;

    @Transactional
    public Page savePageContent(Site site, String url) throws IOException {
        Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new IOException("Ошибка при подключении к URL: " + url, e);
        }

        String htmlContent = doc.html();

        Page page = new Page();
        page.setSite(site);
        page.setPath(url);
        page.setCode(200);
        page.setContent(htmlContent);

        pageRepository.save(page);

        return page;
    }
}
package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Integer> {

    // Метод для поиска сайта по URL
    Site findByUrl(String url);

    // Метод для поиска сайтов по статусу
    List<Site> findByStatus(SiteStatus status);
}
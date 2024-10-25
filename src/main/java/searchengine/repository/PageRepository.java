package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    boolean existsByPath(String path);

    List<Page> findBySite(Site site);

    @Query("SELECT p FROM Page p JOIN Index i ON i.page = p WHERE i.lemma = :lemma AND (:site IS NULL OR p.site.url = :site)")
    List<Page> findPagesByLemma(@Param("lemma") Lemma lemma, @Param("site") String site);
}
package searchengine.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.model.Lemma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    @Query("SELECT l FROM Lemma l WHERE l.lemma IN :lemmas")
    List<Lemma> findAllByLemmaIn(@Param("lemmas") Collection<String> lemmas);

    Optional<Lemma> findByLemmaAndSiteId(String lemma, Long siteId);
}
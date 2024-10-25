package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    Optional<Double> findRankByPageAndLemma(Page page, Lemma lemma);
}
package searchengine.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "lemma")
@Data
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "site_id", nullable = false)
    private int siteId;

    @Column(name = "lemma", nullable = false, length = 255)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private int frequency;
}
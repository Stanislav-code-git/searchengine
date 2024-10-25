package searchengine.model;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "page", indexes = { @Index(name = "path_idx", columnList = "path") })
@Data
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "path", nullable = false, length = 255)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;
}
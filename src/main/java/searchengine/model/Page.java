package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "page", indexes = {@Index(name = "path_index", columnList = "path")})
@NoArgsConstructor
@Setter
@Getter
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "site_id", columnDefinition = "INTEGER")
    private int siteId;
    private String path;
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false, insertable = false, updatable = false)
    private SitePage sitePage;
}
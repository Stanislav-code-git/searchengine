package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "page", indexes = @Index(columnList = "path"))
@NoArgsConstructor
@Getter
@Setter
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "path", columnDefinition = "VARCHAR(255)", nullable = false)
    private String path;

    @Column(name = "code", nullable = false)
    private int code;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
}
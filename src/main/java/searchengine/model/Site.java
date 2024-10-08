package searchengine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "site")
@NoArgsConstructor
@Getter
@Setter
public class Site {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
        @Enumerated(EnumType.STRING)
        private SiteStatus status;

        @Column(name = "status_time", nullable = false)
        private LocalDateTime statusTime;

        @Column(name = "last_error", columnDefinition = "TEXT")
        private String lastError;

        @Column(name = "url", columnDefinition = "VARCHAR(255)", nullable = false)
        private String url;

        @Column(name = "name", columnDefinition = "VARCHAR(255)", nullable = false)
        private String name;

        @OneToMany(mappedBy = "site", cascade = CascadeType.ALL)
        private List<Page> pages;
}
package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "site")
@NoArgsConstructor
@Setter
@Getter
public class SitePage {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(columnDefinition = "INTEGER")
        private int id;
        @Enumerated(EnumType.STRING)
        private Status status;
        @Column(name = "status_time")
        private Timestamp statusTime;
        @Column(name = "last_error")
        private String lastError = null;
        @Column(columnDefinition = "VARCHAR(255)")
        private String url;
        @Column(columnDefinition = "VARCHAR(255)")
        private String name;
        @OneToMany(mappedBy = "sitePage", orphanRemoval = true)
        private List<Page> pages;
        @OneToMany(orphanRemoval = true)
        @JoinColumn(name = "site_id")
        private List<Lemma> lemmas;
}
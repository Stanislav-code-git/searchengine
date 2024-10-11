package searchengine.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "site")
@NoArgsConstructor
@Data
public class Site {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
        private Status status;

        @Column(name = "status_time", nullable = false)
        private LocalDateTime statusTime;

        @Column(name = "last_error", columnDefinition = "TEXT")
        private String lastError;

        @Column(nullable = false, columnDefinition = "VARCHAR(255)")
        private String url;

        @Column(nullable = false, columnDefinition = "VARCHAR(255)")
        private String name;

        @OneToMany(mappedBy = "site", cascade = CascadeType.ALL)
        private List<Page> pages;
}
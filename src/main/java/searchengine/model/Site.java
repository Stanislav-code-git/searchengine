package searchengine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "site")
@NoArgsConstructor
@Data
public class Site {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        // Исправление: Использование @Enumerated для корректной работы с Enum
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
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
        private Set<Page> pages;
}
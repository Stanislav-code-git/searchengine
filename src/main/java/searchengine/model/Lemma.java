package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "lemma", uniqueConstraints = @UniqueConstraint(columnNames = {"lemma","site_id"}))
@NoArgsConstructor
@Setter
@Getter
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int frequency;
    @Column(columnDefinition = "VARCHAR(255)")
    private String lemma;
    @Column(name = "site_id")
    private int siteId;
    @ManyToOne(cascade = CascadeType.REMOVE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "site_id", insertable = false, updatable = false, nullable = false)
    private SitePage sitePage;

}
package zipdabang.server.domain;

import javax.persistence.*;

@Entity
public class WeekBestRecipes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipes recipes;
}

package zipdabang.server.domain;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Recipes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private Boolean is_influencer;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String thumbnail_url;

    @Lob
    private String intro;

    private float star;

    private Integer total_view;

    private Integer total_like;

    private Integer total_scrap;

    private Integer week_view;

    private Integer week_like;

    private Integer week_scrap;

    @Temporal(value = TemporalType.DATE)
    private Date created_at;

    //updated_at

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "recipes")
    @JoinColumn(name = "recipe_id", nullable = false)
    private List<Comments> commentsList;

    @OneToMany(mappedBy = "recipes")
    private List<Likes> likesList;

    @OneToMany(mappedBy = "recipes")
    private List<Scraps> scrapsList;

    @OneToMany(mappedBy = "recipes")
    private List<Steps> stepsList;

    @OneToMany(mappedBy = "recipes")
    private List<Ingredients> ingredientsList;

    @OneToOne(mappedBy = "recipes", cascade = CascadeType.ALL)
    private WeekBestRecipes weekBestRecipes;
}

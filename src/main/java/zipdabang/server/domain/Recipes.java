package zipdabang.server.domain;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Recipes extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private Boolean isInfluencer;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    private String intro;

    private Float star;
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long total_view;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long total_like;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long total_scrap;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long week_view;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long week_like;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long week_scrap;


    //updated_at

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "recipes")
    private List<Comments> commentsList;

    @OneToMany(mappedBy = "recipes", cascade = CascadeType.ALL)
    private List<Likes> likesList;

    @OneToMany(mappedBy = "recipes", cascade = CascadeType.ALL)
    private List<Scraps> scrapsList;

    @OneToMany(mappedBy = "recipes", cascade = CascadeType.ALL)
    private List<Steps> stepsList;

    @OneToMany(mappedBy = "recipes", cascade = CascadeType.ALL)
    private List<Ingredients> ingredientsList;

    @OneToOne(mappedBy = "recipes", cascade = CascadeType.ALL)
    private WeekBestRecipes weekBestRecipes;
}

package zipdabang.server.domain;

import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Entity
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
    private Long totalView;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long totalLike;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long totalScrap;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long weekView;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long weekLike;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long weekScrap;

    //updated_at

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "recipes")
    private List<Comments> commentsList;

    @OneToMany(mappedBy = "recipes")
    private List<Likes> likesList;

    @OneToMany(mappedBy = "recipes")
    private List<Scraps> scrapsList;

    @OneToMany(mappedBy = "recipes")
    private List<Steps> stepsList;

    @OneToMany(mappedBy = "recipes", cascade = CascadeType.ALL)
    private List<Ingredients> ingredientsList;

    @OneToOne(mappedBy = "recipes", cascade = CascadeType.ALL)
    private WeekBestRecipes weekBestRecipes;
}

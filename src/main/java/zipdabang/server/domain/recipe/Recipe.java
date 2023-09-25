package zipdabang.server.domain.recipe;

import javax.persistence.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.member.Member;

import java.util.List;

@Slf4j
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Entity
public class Recipe extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Boolean isInfluencer;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String thumbnailUrl;

    @Column(length = 500, nullable = false)
    private String intro;

    @Column(length = 500, nullable = false)
    private String recipeTip;

    @Column(nullable = false)
    private String time;

    @Column(columnDefinition = "FLOAT DEFAULT 0")
    private Float starScore;
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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<RecipeCategoryMapping> categoryMappingList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Comment> commentList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Likes> likesList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Scrap> scrapList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Step> stepList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Ingredient> ingredientList;

    @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL)
    private WeeklyBestRecipe weeklyBestRecipe;


    public Recipe setThumbnail(String imageUrl) {
        this.thumbnailUrl = imageUrl;
        return this;
    }

    public Recipe updateLike(Integer i){
        this.weekLike += i;
        this.totalLike += i;
        return this;
    }

    public Recipe updateScrap(Integer i){
        this.weekScrap += i;
        this.totalScrap += i;
        return this;
    }

    public Recipe updateView(){
        this.weekView += 1;
        this.totalView += 1;
        log.info("totalLike= ", this.totalView, ", weekView= ", this.weekView);
        return this;
    }
}

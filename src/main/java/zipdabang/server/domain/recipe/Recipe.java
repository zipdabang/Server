package zipdabang.server.domain.recipe;

import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.market.member.Member;

import java.util.List;

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

    private Boolean isInfluencer;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(length = 500)
    private String intro;

    @Column(length = 500)
    private String recipeTip;

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

    @OneToMany(mappedBy = "recipe")
    private List<Scrap> scrapList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Step> stepList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Ingredient> ingredientList;

    @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL)
    private WeeklyBestRecipe weeklyBestRecipe;

    public void addIngredient(Ingredient ingredient){
        this.ingredientList.add(ingredient);
    }

    public void addStep(Step step) {
        this.stepList.add(step);
    }

    public void addCategory(RecipeCategoryMapping categoryMapping) {
        this.categoryMappingList.add(categoryMapping);
    }
}

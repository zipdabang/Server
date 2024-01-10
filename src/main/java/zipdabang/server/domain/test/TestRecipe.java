package zipdabang.server.domain.test;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import javax.persistence.*;
import java.util.List;

@Slf4j
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Entity
public class TestRecipe extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Boolean isBarista;

    @Column(columnDefinition = "boolean default false", nullable = false)
    private Boolean isOfficial;

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

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long totalComments;

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
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", nullable = false)
//    private Member member;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<TestRecipeCategoryMapping> categoryMappingList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<TestComment> commentList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<TestLikes> likesList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<TestScrap> scrapList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<TestStep> stepList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<TestIngredient> ingredientList;

//    @OneToMany(mappedBy = "targetRecipe", cascade = CascadeType.ALL)
//    private List<PushAlarm> pushAlarmList;


    public TestRecipe setThumbnail(String imageUrl) {
        this.thumbnailUrl = imageUrl;
        return this;
    }

    public TestRecipe updateComment(Integer i) {
        this.totalComments += i;
        return this;
    }

    public TestRecipe updateLike(Integer i){
        this.weekLike += i;
        if (weekLike < 0)
            this.weekLike = 0L;

        this.totalLike += i;
        return this;
    }

    public TestRecipe updateScrap(Integer i){
        this.weekScrap += i;
        if (weekScrap < 0)
            this.weekScrap = 0L;

        this.totalScrap += i;
        return this;
    }

    public TestRecipe updateView(){
        this.weekView += 1;
        this.totalView += 1;
        log.info("totalLike= ", this.totalView, ", weekView= ", this.weekView);
        return this;
    }

    public TestRecipe updateInfo(RecipeRequestDto.UpdateRecipeDto request) {
        this.name = request.getName();
        this.intro = request.getIntro();
        this.recipeTip = request.getRecipeTip();
        this.time = request.getTime();

        return this;
    }
}

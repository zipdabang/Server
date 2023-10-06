package zipdabang.server.domain.recipe;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.converter.RecipeConverter;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.member.Member;
import zipdabang.server.web.dto.requestDto.RecipeRequestDto;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;

@Slf4j
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Entity
public class TempRecipe extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Boolean isBarista;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(length = 500)
    private String intro;

    @Column(length = 500)
    private String recipeTip;

    private String time;


    //updated_at

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "tempRecipe", cascade = CascadeType.ALL)
    private List<TempStep> stepList;

    @OneToMany(mappedBy = "tempRecipe", cascade = CascadeType.ALL)
    private List<TempIngredient> ingredientList;


    public TempRecipe setThumbnail(String imageUrl) {
        log.info("setThumbnail 호출됨");
        this.thumbnailUrl = imageUrl;
        return this;
    }

    public TempRecipe updateInfo(RecipeRequestDto.TempRecipeDto request) {
        this.name = request.getName();
        this.intro = request.getIntro();
        this.recipeTip = request.getRecipeTip();
        this.time = request.getTime();

        return this;
    }
}

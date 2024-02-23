package zipdabang.server.domain.test;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.member.Member;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class TestScrap extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private TestRecipe recipe;

    public TestScrap deleteScrap(TestRecipe recipe){
        if(this.recipe != null)
            recipe.getScrapList().remove(this);
        recipe.updateScrap(-1);
        return this;
    }

    public TestScrap setRecipe(TestRecipe recipe){

        recipe.updateScrap(1);

        if(this.recipe != null)
            recipe.getScrapList().remove(this);
        this.recipe = recipe;
        recipe.getScrapList().add(this);

        return this;
    }
}

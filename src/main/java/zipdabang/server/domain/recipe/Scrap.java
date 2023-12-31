package zipdabang.server.domain.recipe;

import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.member.Member;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Scrap extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    public Scrap deleteScrap(Recipe recipe){
        if(this.recipe != null)
            recipe.getScrapList().remove(this);
        recipe.updateScrap(-1);
        return this;
    }

    public Scrap setRecipe(Recipe recipe){

        recipe.updateScrap(1);

        if(this.recipe != null)
            recipe.getScrapList().remove(this);
        this.recipe = recipe;
        recipe.getScrapList().add(this);

        return this;
    }
}

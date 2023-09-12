package zipdabang.server.domain.recipe;

import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.common.BaseEntity;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Likes extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public Likes deleteLikes(Recipe recipe){
        if(this.recipe != null)
            recipe.getLikesList().remove(this);
        recipe.updateLike(-1);
        return this;
    }

    public Likes setRecipe(Recipe recipe){

        recipe.updateLike(1);

        if(this.recipe != null)
            recipe.getLikesList().remove(this);
        this.recipe = recipe;
        recipe.getLikesList().add(this);

        return this;
    }
}

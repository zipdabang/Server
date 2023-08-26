package zipdabang.server.domain.recipe;

import javax.persistence.*;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.recipe.Recipe;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Step extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private Integer stepNum;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public Step setImage(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public Step setRecipe(Recipe recipe){
        if(this.recipe != null)
            recipe.getStepList().remove(this);
        this.recipe = recipe;
        recipe.getStepList().add(this);

        return this;
    }
}

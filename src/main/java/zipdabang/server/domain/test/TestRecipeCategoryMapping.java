package zipdabang.server.domain.test;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.recipe.RecipeCategory;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
@Entity
public class TestRecipeCategoryMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private TestRecipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_category_id", nullable = false)
    private RecipeCategory category;

    public TestRecipeCategoryMapping setRecipe(TestRecipe recipe){
        if(this.recipe != null)
            recipe.getCategoryMappingList().remove(this);
        this.recipe = recipe;
        recipe.getCategoryMappingList().add(this);

        return this;
    }
}

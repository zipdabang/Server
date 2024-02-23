package zipdabang.server.domain.test;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class TestStep extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Integer stepNum;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private TestRecipe recipe;

    public TestStep setImage(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public TestStep setRecipe(TestRecipe recipe){
        if(this.recipe != null)
            recipe.getStepList().remove(this);
        this.recipe = recipe;
        recipe.getStepList().add(this);

        return this;
    }
}
package zipdabang.server.domain.recipe;

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
public class TempIngredient extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String name;

    private String quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temp_id", nullable = false)
    private TempRecipe tempRecipe;

    public TempIngredient setTempRecipe(TempRecipe tempRecipe){
        if(this.tempRecipe != null)
            tempRecipe.getIngredientList().remove(this);
        this.tempRecipe = tempRecipe;
        tempRecipe.getIngredientList().add(this);

        return this;
    }
}

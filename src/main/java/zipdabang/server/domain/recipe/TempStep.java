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
public class TempStep extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Integer stepNum;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temp_id", nullable = false)
    private TempRecipe tempRecipe;

    public TempStep setImage(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public TempStep setTempRecipe(TempRecipe tempRecipe){
        if(this.tempRecipe != null)
            tempRecipe.getStepList().remove(this);
        this.tempRecipe = tempRecipe;
        tempRecipe.getStepList().add(this);

        return this;
    }
}

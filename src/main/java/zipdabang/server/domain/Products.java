package zipdabang.server.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Products extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String thumbnailUrl;

    private Integer price;

    @Column(length=500)
    private String intro;

    private String description;

    private Boolean is_kit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Categories categories;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kit_category_id")
    private KitCategories kitCategories;

    public Products update(){
        return this;
    }
}

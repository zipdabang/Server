package zipdabang.server.domain.market;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.common.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String thumbnailUrl;

    @Column(length=500)
    private String intro;
    private String description;
    private Integer price;
    private String origin;
    private String storage;
    private LocalDateTime makedAt;
    private Long reviews;

    private Long likes;
    private Long weekSale;
    private Float starScore;

    @Column(columnDefinition = "TEXT")
    private String inKit;
    private Boolean isKit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_category_id")
    private MarketCategory marketCategory;

    public Product update(){
        return this;
    }
}

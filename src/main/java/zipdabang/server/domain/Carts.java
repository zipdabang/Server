package zipdabang.server.domain;

import lombok.*;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Carts extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Products product;

    private String name;

    private String thumbUrl;

    private int price;

    private int count;

    private boolean is_shown;

    public Carts updateCount(int count){
        this.count = count;
        return this;
    }
}

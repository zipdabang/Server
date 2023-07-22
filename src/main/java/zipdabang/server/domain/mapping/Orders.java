package zipdabang.server.domain.mapping;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.Users;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.enums.OrderStates;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Enumerated(EnumType.STRING)
    private OrderStates orderState;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupons coupon;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "payment_id")
//    private Payments payment;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "recieve_info_id")
//    private RecieveInfos recieveInfo;

    private Integer price;

    private Integer total_price;
}
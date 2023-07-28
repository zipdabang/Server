package zipdabang.server.domain.mapping;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.Member;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.enums.OrderState;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private OrderState orderState;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

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

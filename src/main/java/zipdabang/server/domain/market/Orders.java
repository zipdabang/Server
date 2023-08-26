package zipdabang.server.domain.market;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.common.BaseEntity;
import zipdabang.server.domain.enums.OrderState;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState status;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String requirement;

    @Column(nullable = false)
    private String totalPrice;

    @Column(nullable = false)
    private LocalDate orderDate;

    private LocalDate returnDate;

    //    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "coupon_id")
//    private Coupon coupon;

    //    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "payment_id")
//    private Payments payment;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "recieve_info_id")
//    private RecieveInfos recieveInfo;
}

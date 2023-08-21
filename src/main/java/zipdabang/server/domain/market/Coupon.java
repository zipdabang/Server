package zipdabang.server.domain.market;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import zipdabang.server.domain.market.member.Member;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@DynamicUpdate
public class Coupon extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String name;

    private Float discountRate;

    @Column(columnDefinition = "boolean default true")
    private Boolean isUsable;
    //이거 builder 패턴 사용해서 default 값 true로 할 수 있다는데

    private LocalDateTime expirationDate;

    private LocalDateTime usedDate;

    public Coupon couponUsed(){
        this.isUsable = false;
        this.usedDate = LocalDateTime.now();
        return this;
    }

    public Coupon cancelCouponUsed(){
        this.isUsable = true;
        this.usedDate = null;
        return this;
    }

    public Coupon couponExpired(){
        this.isUsable = false;
        return this;
    }

}

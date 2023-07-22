package zipdabang.server.domain.mapping;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import zipdabang.server.domain.Users;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@DynamicUpdate
public class Coupons extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    private String name;

    private Float discountRate;

    @Column(columnDefinition = "boolean default true")
    private Boolean isUsable;
    //이거 builder 패턴 사용해서 default 값 true로 할 수 있다는데

    private LocalDateTime expirationDate;

    private LocalDateTime usedDate;

    public Coupons couponUsed(){
        this.isUsable = false;
        this.usedDate = LocalDateTime.now();
        return this;
    }

    public Coupons cancelCouponUsed(){
        this.isUsable = true;
        this.usedDate = null;
        return this;
    }

    public Coupons couponExpired(){
        this.isUsable = false;
        return this;
    }

}

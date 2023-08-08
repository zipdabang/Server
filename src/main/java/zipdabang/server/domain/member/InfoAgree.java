package zipdabang.server.domain.member;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@DynamicInsert
@DynamicUpdate
@Entity
public class InfoAgree extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private Boolean infoOthersAgree;

    private Boolean infoAgreeBoolean;

    private LocalDateTime infoAgreeDate;

    @OneToOne
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    public InfoAgree update(Boolean infoAgreeBoolean, Boolean infoOthersAgree, Member member) {
        this.infoAgreeBoolean = infoAgreeBoolean;
        this.infoOthersAgree =infoOthersAgree;
        this.infoAgreeDate = LocalDateTime.now();
        this.member=member;

        return this;
    }
}

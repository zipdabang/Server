package zipdabang.server.domain.market.member;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@DynamicInsert
@DynamicUpdate
@Entity
public class TermsAgree extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private LocalDate infoAgreeDate;

    @ManyToOne
    @JoinColumn(name="member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "terms_id")
    private Terms terms;

    public void setMember(Member member) {
        if(this.member != null) {
            member.getTermsAgree().remove(this);
        }
        this.member = member;
        member.getTermsAgree().add(this);
    }

    public void setTerms(Terms terms){
        if(this.terms != null) {
            terms.getTermsAgreeList().remove(this);
        }
        this.terms = terms;
        terms.getTermsAgreeList().add(this);
    }
}

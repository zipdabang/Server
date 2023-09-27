package zipdabang.server.domain.member;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@DynamicInsert
@DynamicUpdate
@Entity
public class Inquery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String receiveEmail;

    @Column(nullable = false,length = 20)
    private String title;

    @Column(nullable = false,length = 500)
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "inquery", cascade = CascadeType.ALL)
    private List<InqueryImage> inqueryImageList = new ArrayList<>();

    public void setMember(Member member){
        if(this.member != null)
            member.getInqueryList().remove(this);
        this.member = member;
        member.getInqueryList().add(this);
    }
}

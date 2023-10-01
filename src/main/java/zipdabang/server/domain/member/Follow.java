package zipdabang.server.domain.member;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import zipdabang.server.domain.common.BaseEntity;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Follow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // follow 객체
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    private Member targetMember;

    // follow 주체
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private Member followingMember;

    public void setTargetMember(Member targetMember){
        if (this.targetMember != null)
            targetMember.getFollowerList().remove(this);
        this.targetMember = targetMember;
        targetMember.getFollowerList().add(this);
    }

    public void setFollowingMember(Member followingMember){
        if (this.followingMember != null)
            followingMember.getFollowerList().remove(this);
        this.followingMember = followingMember;
        followingMember.getFollowerList().add(this);
    }

}

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

    // 팔로우 당하는 대상
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id")
    private Member followee;

    // 팔로우 하는 놈
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private Member follower;

    // 팔로우 하는놈 세팅
    public void setFollower(Member follower){
        if (this.follower != null)
            follower.getMyFollowingList().remove(this);
        this.follower = follower;
        follower.getMyFollowingList().add(this);
    }

    public void setFollowee(Member followee){
        if(this.followee != null)
            followee.getMyFollowerList().remove(this);
        this.followee = followee;
        followee.getMyFollowerList().add(this);
    }

    public void cancleFollow(Member followee, Member follower){
        this.follower = null;
        this.followee = null;
        followee.getMyFollowerList().remove(this);
        follower.getMyFollowingList().remove(this);
    }
}

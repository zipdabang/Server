package zipdabang.server.repository.memberRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import zipdabang.server.domain.member.Follow;
import zipdabang.server.domain.member.Member;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Page<Follow> findAllByFollowee(Member member, PageRequest pageRequest);
    Long countByFollowee(Member followee);

    Page<Follow> findAllByFollower(Member member, PageRequest pageRequest);

    Long countByFollower(Member follower);
    Optional<Follow> findByFollowerAndFollowee(Member follower, Member followee);

    boolean existsByFollowerAndFollowee(Member follower, Member followee);

}

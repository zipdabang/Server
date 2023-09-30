package zipdabang.server.repository.memberRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import zipdabang.server.domain.member.Follow;
import zipdabang.server.domain.member.Member;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Page<Follow> findAllByTargetMember(Member member, PageRequest pageRequest);
    Page<Follow> findAllByFollowingMember(Member member, PageRequest pageRequest);
}

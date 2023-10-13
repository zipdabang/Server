package zipdabang.server.repository.memberRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zipdabang.server.domain.enums.StatusType;
import zipdabang.server.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);
    Page<Member> findByNicknameContains(String nickname, PageRequest pageRequest);

    @Query("select m from Member m where m.nickname like concat('%',:nickname,'%') and m in ( select f.follower from Follow f where f.followee = :owner)")
    Page<Member> qFindFollowerByNicknameContains(@Param("nickname")String nickname, @Param("owner")Member member, PageRequest pageRequest);
    @Query("select m from Member m where m.nickname like concat('%',:nickname,'%') and m in ( select f.followee from Follow f where f.follower = :owner)")
    Page<Member> qFindFollowingByNicknameContains(@Param("nickname")String nickname, @Param("owner")Member member, PageRequest pageRequest);

    boolean existsByPhoneNum(String phoneNum);
}

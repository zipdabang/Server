package zipdabang.server.repository.memberRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.enums.StatusType;
import zipdabang.server.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);
    Page<Member> findByNicknameContains(String nickname, PageRequest pageRequest);

    boolean existsByPhoneNum(String phoneNum);
}

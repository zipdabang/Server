package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}

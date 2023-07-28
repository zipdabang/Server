package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}

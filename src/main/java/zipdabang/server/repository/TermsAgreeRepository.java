package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.TermsAgree;

public interface TermsAgreeRepository extends JpaRepository<TermsAgree, Long> {
}

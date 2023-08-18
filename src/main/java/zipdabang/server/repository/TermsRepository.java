package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Terms;

import java.util.List;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findByIdIn(List<Long> idList);
}

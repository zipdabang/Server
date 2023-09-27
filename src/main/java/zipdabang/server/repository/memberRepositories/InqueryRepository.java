package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Inquery;

public interface InqueryRepository extends JpaRepository<Inquery, Long> {
}

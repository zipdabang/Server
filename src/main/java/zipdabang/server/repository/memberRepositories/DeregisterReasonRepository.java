package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zipdabang.server.domain.member.DeregisterReason;

public interface DeregisterReasonRepository extends JpaRepository<DeregisterReason, Long> {

}

package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Deregister;

public interface DeregisterRepository extends JpaRepository<Deregister, Long> {

}

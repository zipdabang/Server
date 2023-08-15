package zipdabang.server.redis.repository;

import org.springframework.data.repository.CrudRepository;
import zipdabang.server.redis.domain.LoginStatus;

public interface LoginStatusRepository extends CrudRepository<LoginStatus, String> {
}

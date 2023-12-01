package zipdabang.server.redis.repository;

import org.springframework.data.repository.CrudRepository;
import zipdabang.server.redis.domain.PhoneAuth;

public interface PhoneAuthRepository extends CrudRepository<PhoneAuth, String> {

}

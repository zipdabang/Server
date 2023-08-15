package zipdabang.server.redis.repository;

import org.springframework.data.repository.CrudRepository;
import zipdabang.server.redis.domain.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}

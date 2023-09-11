package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.etc.Uuid;

public interface UuidRepository extends JpaRepository<Uuid, Long> {
    boolean existsByUuid(String uuid);

    void deleteByUuid(String uuid);
}

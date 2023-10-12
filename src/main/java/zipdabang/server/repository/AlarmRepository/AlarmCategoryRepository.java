package zipdabang.server.repository.AlarmRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.enums.AlarmType;
import zipdabang.server.domain.inform.AlarmCategory;

import java.util.Optional;

public interface AlarmCategoryRepository extends JpaRepository<AlarmCategory, Long> {

    Optional<AlarmCategory> findByName(AlarmType name);
}

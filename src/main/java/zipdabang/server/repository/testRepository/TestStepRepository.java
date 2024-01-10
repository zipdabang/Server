package zipdabang.server.repository.testRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.test.TestStep;

public interface TestStepRepository extends JpaRepository<TestStep, Long> {
}

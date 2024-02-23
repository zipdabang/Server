package zipdabang.server.repository.testRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.test.TestIngredient;

public interface TestIngredientRepository extends JpaRepository<TestIngredient,Long> {
}

package zipdabang.server.repository.testRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.test.TestRecipe;

public interface TestRecipeRepository extends JpaRepository<TestRecipe, Long>{}

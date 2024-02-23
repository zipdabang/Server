package zipdabang.server.repository.testRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.Step;
import zipdabang.server.domain.test.TestStep;

import java.util.List;

public interface TestStepRepository extends JpaRepository<TestStep, Long> {
    List<Step> findAllByRecipeId(Long recipeId);
}

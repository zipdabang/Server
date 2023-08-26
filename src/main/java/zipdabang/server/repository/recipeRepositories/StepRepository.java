package zipdabang.server.repository.recipeRepositories;


import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.recipe.Step;

public interface StepRepository extends JpaRepository<Step, Long> {
}

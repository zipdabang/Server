package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

}

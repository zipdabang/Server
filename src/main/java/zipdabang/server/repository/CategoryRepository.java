package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}

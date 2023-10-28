package zipdabang.server.repository.recipeRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.inform.HomeBanner;

public interface HomeBannerRepository extends JpaRepository<HomeBanner, Long> {
}

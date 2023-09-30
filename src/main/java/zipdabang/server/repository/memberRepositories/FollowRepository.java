package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.Follow;

public interface FollowRepository extends JpaRepository<Follow, Long> {
}

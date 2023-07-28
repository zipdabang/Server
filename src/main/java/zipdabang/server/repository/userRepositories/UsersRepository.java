package zipdabang.server.repository.userRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
}

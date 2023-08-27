package zipdabang.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.etc.AuthNumber;

import java.util.Optional;

public interface AuthNumberRepository extends JpaRepository<AuthNumber, Long> {
    Optional<AuthNumber> findByPhoneNum(String phoneNum);
    void deleteByPhoneNum(String phoneNum);
}

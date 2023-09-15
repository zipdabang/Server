package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.member.FcmToken;
import zipdabang.server.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByTokenAndSerialNumber(String token, String serialNumber);

    List<FcmToken> findAllByMember(Member member);
}

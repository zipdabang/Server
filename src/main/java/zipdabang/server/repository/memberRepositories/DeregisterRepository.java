package zipdabang.server.repository.memberRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.member.Deregister;

public interface DeregisterRepository extends JpaRepository<Deregister, Long> {
    boolean existsByPhoneNumAndPassedSevenDays(String phoneNum,boolean passedSevenDays);

    boolean existsByEmailAndSocialTypeAndPassedSevenDays(String email, SocialType socialType,boolean passedSevenDays);

}

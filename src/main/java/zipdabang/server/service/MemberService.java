package zipdabang.server.service;

import zipdabang.server.domain.member.Member;
import zipdabang.server.utils.OAuthResult;

import java.util.Optional;

public interface MemberService {
    OAuthResult.OAuthResultDto kakaoSocialLogin(String email, String profileUrl);

    Optional<Member> checkExistNickname(String nickname);
}

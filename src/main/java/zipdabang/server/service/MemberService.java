package zipdabang.server.service;

import zipdabang.server.utils.OAuthResult;

public interface MemberService {
    OAuthResult.OAuthResultDto kakaoSocialLogin(String email, String profileUrl, String type);
}

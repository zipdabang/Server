package zipdabang.server.service;

import zipdabang.server.domain.Category;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.Terms;
import zipdabang.server.redis.domain.RefreshToken;
import zipdabang.server.utils.dto.OAuthJoin;
import zipdabang.server.utils.dto.OAuthResult;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;

import java.util.List;
import java.util.Optional;

public interface MemberService {
    OAuthResult.OAuthResultDto SocialLogin(MemberRequestDto.OAuthRequestDto request, String type);

    Optional<Member> checkExistNickname(String nickname);

    public void existByPhoneNumber(String phoneNum);
    
    OAuthJoin.OAuthJoinDto joinInfoComplete(MemberRequestDto.MemberInfoDto request, String type);

    List<Category> getCategoryList();

    void logout(String accessToken, MemberRequestDto.LogoutDto request);

    String regenerateAccessToken(RefreshToken refreshToken);

    List<Terms> getAllTerms();
}

package zipdabang.server.service;

import zipdabang.server.domain.Category;
import zipdabang.server.domain.member.Member;
import zipdabang.server.utils.dto.OAuthJoin;
import zipdabang.server.utils.dto.OAuthResult;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;

import java.util.List;
import java.util.Optional;

public interface MemberService {
    OAuthResult.OAuthResultDto SocialLogin(String email, String profileUrl, String type);

    Optional<Member> checkExistNickname(String nickname);
    
    OAuthJoin.OAuthJoinDto joinInfoComplete(MemberRequestDto.MemberInfoDto request, String type);

    List<Category> getCategoryList();

    void logout(String accessToken);
}

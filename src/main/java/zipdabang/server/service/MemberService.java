package zipdabang.server.service;

import zipdabang.server.domain.member.Member;
import zipdabang.server.utils.OAuthResult;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;

import java.util.Optional;

public interface MemberService {
    OAuthResult.OAuthResultDto kakaoSocialLogin(String email, String profileUrl);

    Optional<Member> checkExistNickname(String nickname);
    
    Member joinInfoComplete(MemberRequestDto.MemberInfoDto request, Member member);
}

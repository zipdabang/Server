package zipdabang.server.service;

import zipdabang.server.domain.Category;
import zipdabang.server.domain.member.Inquery;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.Terms;
import zipdabang.server.redis.domain.RefreshToken;
import zipdabang.server.utils.dto.OAuthJoin;
import zipdabang.server.utils.dto.OAuthResult;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface MemberService {
    OAuthResult.OAuthResultDto SocialLogin(MemberRequestDto.OAuthRequestDto request, String type);

    Optional<Member> checkExistNickname(String nickname);

    public void existByPhoneNumber(String phoneNum);
    
    OAuthJoin.OAuthJoinDto joinInfoComplete(MemberRequestDto.MemberInfoDto request, String type);
    public List<Category> findMemberPreferCategories(Member member);

    public void deletePreferCategoryByMember(Member member);
    public void updateMemberPreferCategory(Member member, MemberRequestDto.changeCategoryDto request);
    public String updateMemberProfileImage(Member member, MemberRequestDto.changeProfileDto profileDto) throws IOException;
    public void updateMemberBasicInfo(Member member, MemberResponseDto.MemberBasicInfoDto memberBasicInfoDto);

    public void updateMemberDetailInfo(Member member, MemberResponseDto.MemberDetailInfoDto memberDetailInfoDto);
    public void updateMemberNickname(Member member, String newNickname);

    void logout(String accessToken, Member member);

    String regenerateAccessToken(RefreshToken refreshToken);

    List<Terms> getAllTerms();

    String tempLoginService();

    Inquery createInquery(Member member, MemberRequestDto.InqueryDto request);
}

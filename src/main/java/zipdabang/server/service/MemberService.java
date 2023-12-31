package zipdabang.server.service;

import org.springframework.data.domain.Page;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.inform.PushAlarm;
import zipdabang.server.domain.member.*;
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

    Optional<Member> findMemberById(Long id);

    public void existByPhoneNumber(String phoneNum);

    public void joinDeregisterCheck(String email, String phoneNum, SocialType socialType);
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

    Page<Inquery> findInquery(Member member, Integer page);
    public void memberDeregister(Member member, MemberRequestDto.DeregisterDto request);
    public Long saveDeregisterInfo(Member member, MemberRequestDto.DeregisterDto request);
    public void inactivateMember(Member member);


    public void blockMember(Member owner, Long blocked);
    public void unblockMember(Member owner, Long blockedId);
    public Page<Member> findBlockedMember(Integer page, Member member);

    public Follow toggleFollow(Long targetId, Member member);

    Page<Follow> findFollowing(Member member, Integer page);
    public Long getFollowingCount(Member member);

    Page<Follow> findFollower(Member member, Integer page);
    public Long getFollowerCount(Member member);

    public void updateCaption(Member member, MemberRequestDto.changeCaptionDto captionDto);
    public void updateProfileDefault(Member member);
    Boolean checkFollowing(Member loginMember, Member targetMember);
    public MemberResponseDto.MyZipdabangDto getMyZipdabang(Member member, Long targetId);
    public MemberResponseDto.MyZipdabangDto getSelfMyZipdabang(Member member);

    Page<PushAlarm> getPushAlarms(Member member, Integer page);
    public Page<Member> findByNicknameContains(Integer page, String nickname);
    public Page<Member> findFollowerByNicknameContains(Integer page, Long targetId, String nickname);
    public Page<Member> findFollowingByNicknameContains(Integer page, Long targetId, String nickname);

    Optional<Inquery> findInqueryById(Long inqueryId);

    Inquery findMyInqueryById(Member member,Long inqueryId);

    MemberReport reportMember(Member member, Long targetId);
}

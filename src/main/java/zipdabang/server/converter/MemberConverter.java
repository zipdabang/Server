package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.MemberException;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.enums.GenderType;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.member.Terms;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.MemberPreferCategory;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.utils.dto.OAuthJoin;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemberConverter {

    private final MemberRepository memberRepository;

    private static MemberRepository staticMemberRepository;

    public static MemberResponseDto.JoinMemberDto toJoinMemberDto(Member member){
            return MemberResponseDto.JoinMemberDto.builder()
                    .memberId(member.getMemberId())
                    .nickname(member.getNickname()).build();
//                .accessToken()
        }

    public static Member toSocialMember(MemberRequestDto.MemberInfoDto request, String type) {

        // 생년월일 문자열을 LocalDate로 변환
        String birth = request.getBirth().substring(0,2);

        // 현재 날짜를 가져옴
        LocalDate currentDate = LocalDate.now();

        // 생년월일과 현재 날짜를 기준으로 만 나이 계산
        int age = currentDate.getYear() % 100 - Integer.valueOf(birth)- 1;

        if (age < 0)
            age += 100;

        // 현재 연도를 가져옴
        int currentYear = LocalDate.now().getYear();

        // 날짜 문자열을 LocalDate로 변환
        LocalDate date = LocalDate.parse(request.getBirth(), DateTimeFormatter.ofPattern("yyMMdd"));

        // 생년월일에 현재 연도를 설정하여 완전한 날짜로 만듦
        LocalDate completeDate = date.withYear(currentYear);

        age = ChronoUnit.DAYS.between(completeDate, currentDate) >= 0 ? age + 1 : age;
        GenderType gender = Integer.valueOf(request.getGender()) % 2 == 0 ? GenderType.WOMAN : GenderType.MAN;

        Member member = Member.builder()
                .age(age)
                .socialType(type.equals("kakao") ? SocialType.KAKAO : SocialType.GOOGLE)
                .email(request.getEmail())
                .profileUrl(request.getProfileUrl())
                .nickname(request.getNickname())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .gender(gender)
                .name(request.getName())
                .phoneNum(request.getPhoneNum())
                .zipCode(request.getZipCode())
                .termsAgree(new ArrayList<>())
                .build();
        return staticMemberRepository.save(member);
    }

    public static MemberResponseDto.SocialJoinDto toSocialJoinDto(OAuthJoin.OAuthJoinDto result) {
        return MemberResponseDto.SocialJoinDto.builder()
                .refreshToken(result.getRefreshToken())
                .accessToken(result.getAccessToken())
                .build();
    }

    @PostConstruct
    public void init() {
        this.staticMemberRepository = this.memberRepository;
    }


    public static MemberResponseDto.MemberProfileDto toMemberProfileDto(Member member) {
        return MemberResponseDto.MemberProfileDto.builder()
//                .name(member.getName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .phoneNum(member.getPhoneNum())
                .profileUrl(member.getProfileUrl())
                .build();
    }

    public static Member toMember(Long memberId){
        return staticMemberRepository.findById(memberId).orElseThrow(()->new MemberException(Code.MEMBER_NOT_FOUND));
    }

    public static Member toOAuthMember(String email, String profileUrl){
        return Member.builder()
                .socialType(SocialType.KAKAO)
                .email(email)
                .profileUrl(profileUrl)
                .build();
    }

    public static MemberResponseDto.SocialLoginDto toSocialLoginDto(String accessToken, String refreshToken){
        return MemberResponseDto.SocialLoginDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public static MemberPreferCategory toMemberPreferCategory(Member member, Category category) {
        return MemberPreferCategory.builder()
                .member(member)
                .category(category)
                .build();
    }

    public static MemberResponseDto.MemberStatusDto toMemberStatusDto(Long memberId, String status){
        return MemberResponseDto.MemberStatusDto.builder()
                .memberId(memberId)
                .status(status)
                .calledAt(LocalDateTime.now())
                .build();
    }

    public static MemberResponseDto.IssueNewTokenDto toIssueNewTokenDto(String accessToken, String refreshToken){
        return MemberResponseDto.IssueNewTokenDto.builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .build();
    }

    public static MemberResponseDto.TermsDto toTermsDto(Terms terms){
        return MemberResponseDto.TermsDto.builder()
                .termsId(terms.getId())
                .termsTitle(terms.getTermsTitle())
                .termsBody(terms.getTermsBody())
                .isMoreToSee(terms.getIsMoreToSee())
                .build();
    }

    public static MemberResponseDto.TermsListDto toTermsDto(List<Terms> termsList){
        List<MemberResponseDto.TermsDto> termsBodyList = termsList.stream()
                .map(terms -> toTermsDto(terms)).collect(Collectors.toList());
        return MemberResponseDto.TermsListDto.builder()
                .termsList(termsBodyList)
                .size(termsBodyList.size())
                .build();
    }
}

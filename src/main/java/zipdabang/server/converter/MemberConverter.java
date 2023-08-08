package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.MemberException;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.enums.GenderType;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.member.InfoAgree;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.MemberPreferCategory;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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

    public static Member toSocialMember(MemberRequestDto.MemberInfoDto request, Member member) {

        String birthString = request.getBirth();

        String YY = birthString.substring(0, 2);
        String MMDD = birthString.substring(2);

        LocalDate currentDate = LocalDate.now();

        //만나이 계산
        int age = currentDate.getYear() % 100 - Integer.valueOf(YY) - 1;

        if(age < 0)
            age += 100;

        int currentYear = LocalDate.now().getYear();

        LocalDate date = LocalDate.parse(MMDD, DateTimeFormatter.ofPattern("yyMMdd"));
        LocalDate completeDate = date.withYear(currentYear);

        age = ChronoUnit.DAYS.between(completeDate, currentDate) >= 0 ? age + 1 : age;

        GenderType gender = Integer.valueOf(request.getGender()) % 2 == 0 ? GenderType.WOMAN : GenderType.MAN;

        InfoAgree infoAgree = new InfoAgree();
        infoAgree.update(request.getInfoAgree(), request.getInfoOthersAgree(), member);

        return member.updateInfo(request, age, gender, infoAgree);
    }

    public static MemberResponseDto.SocialInfoDto toSocialInfoDto(Member member) {
        return MemberResponseDto.SocialInfoDto.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
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

    public static MemberResponseDto.SocialLoginDto toSocialLoginDto(String jwt){
        return MemberResponseDto.SocialLoginDto.builder()
                .accessToken(jwt)
                .build();
    }

    public static MemberPreferCategory toMemberPreferCategory(Member member, Category category) {
        return MemberPreferCategory.builder()
                .member(member)
                .category(category)
                .build();
    }
}

package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.MemberException;
import zipdabang.server.domain.Member;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import javax.annotation.PostConstruct;

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
}

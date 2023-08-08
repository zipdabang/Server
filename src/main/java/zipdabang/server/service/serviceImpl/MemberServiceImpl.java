package zipdabang.server.service.serviceImpl;

import antlr.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.converter.MemberConverter;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.member.Member;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.service.MemberService;
import zipdabang.server.utils.OAuthResult;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    private final TokenProvider tokenProvider;

    @Override
    @Transactional
    public OAuthResult.OAuthResultDto kakaoSocialLogin(String email, String profileUrl, String type) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if(member != null)
            if (type.equals("kakao")) {
                if(member.getAge() == null || member.getGender() == null)
                    return OAuthResult.OAuthResultDto.builder()
                            .isLogin(false)
                            .memberId(member.getMemberId())
                            .jwt(tokenProvider.createAccessToken(member.getMemberId(), SocialType.KAKAO.toString(), email))
                            .build();
                else
                    return OAuthResult.OAuthResultDto.builder()
                            .isLogin(true)
                            .memberId(member.getMemberId())
                            .jwt(tokenProvider.createAccessToken(member.getMemberId(), SocialType.KAKAO.toString(), email))
                            .build();
            }
            else {
                if(member.getAge() == null || member.getGender() == null)
                    return OAuthResult.OAuthResultDto.builder()
                            .isLogin(false)
                            .memberId(member.getMemberId())
                            .jwt(tokenProvider.createAccessToken(member.getMemberId(), SocialType.GOOGLE.toString(), email))
                            .build();
                else
                    return OAuthResult.OAuthResultDto.builder()
                            .isLogin(true)
                            .memberId(member.getMemberId())
                            .jwt(tokenProvider.createAccessToken(member.getMemberId(), SocialType.GOOGLE.toString(), email))
                            .build();}
        Member newMember = memberRepository.save(MemberConverter.toOAuthMember(email, profileUrl));
        if(type.equals("kakao"))
            return OAuthResult.OAuthResultDto.builder()
                    .isLogin(false)
                    .memberId(newMember.getMemberId())
                    .jwt(tokenProvider.createAccessToken(newMember.getMemberId(), SocialType.KAKAO.toString(), email))
                    .build();
        else
            return OAuthResult.OAuthResultDto.builder()
                    .isLogin(false)
                    .memberId(newMember.getMemberId())
                    .jwt(tokenProvider.createAccessToken(newMember.getMemberId(), SocialType.GOOGLE.toString(), email))
                    .build();
    }
}

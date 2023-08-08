package zipdabang.server.service.serviceImpl;

import antlr.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.MemberException;
import zipdabang.server.converter.MemberConverter;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.MemberPreferCategory;
import zipdabang.server.repository.CategoryRepository;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.repository.memberRepositories.PreferCategoryRepository;
import zipdabang.server.service.MemberService;
import zipdabang.server.utils.OAuthResult;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    private final TokenProvider tokenProvider;

    private final CategoryRepository categoryRepository;
    private final PreferCategoryRepository preferCategoryRepository;

    @Override
    @Transactional
    public OAuthResult.OAuthResultDto kakaoSocialLogin(String email, String profileUrl) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if(member != null)
            return OAuthResult.OAuthResultDto.builder()
                    .isLogin(true)
                    .memberId(member.getMemberId())
                    .jwt(tokenProvider.createAccessToken(member.getMemberId(), SocialType.KAKAO.toString(),email))
                    .build();
        Member newMember = memberRepository.save(MemberConverter.toOAuthMember(email, profileUrl));
        return OAuthResult.OAuthResultDto.builder()
                .isLogin(false)
                .memberId(newMember.getMemberId())
                .jwt(tokenProvider.createAccessToken(newMember.getMemberId(), SocialType.KAKAO.toString(),email))
                .build();
    }

    @Override
    public Optional<Member> checkExistNickname(String nickname){
        return memberRepository.findByNickname(nickname);
    }

    @Override
    public List<Category> getCategoryList(){
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = false)
    public Member joinInfoComplete(MemberRequestDto.MemberInfoDto request, Member member){
        Member joinUser = MemberConverter.toSocialMember(request, member);
        for (int i = 0; i < request.getPreferBeverages().size(); i++) {
            Category category = categoryRepository.findById(Long.valueOf(request.getPreferBeverages().get(i)))
                    .orElseThrow(() -> new MemberException(Code.NO_CATEGORY_EXIST));
            MemberPreferCategory memberPreferCategory = MemberConverter.toMemberPreferCategory(joinUser, category);
            preferCategoryRepository.save(memberPreferCategory);
        }

        return joinUser;
    }
}

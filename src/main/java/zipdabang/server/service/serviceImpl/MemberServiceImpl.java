package zipdabang.server.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.AuthNumberException;
import zipdabang.server.base.exception.handler.MemberException;
import zipdabang.server.converter.MemberConverter;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.enums.DeregisterType;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.etc.Uuid;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.MemberPreferCategory;
import zipdabang.server.domain.member.Terms;
import zipdabang.server.domain.member.TermsAgree;
import zipdabang.server.domain.member.*;
import zipdabang.server.redis.domain.RefreshToken;
import zipdabang.server.repository.TermsAgreeRepository;
import zipdabang.server.repository.TermsRepository;
import zipdabang.server.redis.service.RedisService;
import zipdabang.server.repository.CategoryRepository;
import zipdabang.server.repository.memberRepositories.*;
import zipdabang.server.service.MemberService;
import zipdabang.server.utils.dto.OAuthJoin;
import zipdabang.server.utils.dto.OAuthResult;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    private final TokenProvider tokenProvider;

    private final CategoryRepository categoryRepository;
    private final PreferCategoryRepository preferCategoryRepository;

    private final TermsRepository termsRepository;

    private final TermsAgreeRepository termsAgreeRepository;

    private final RedisService redisService;

    private final FcmTokenRepository fcmTokenRepository;
    private final AmazonS3Manager s3Manager;
    private final DeregisterRepository deregisterRepository;
    private final DeregisterReasonRepository deregisterReasonRepository;

    @Override
    @Transactional
    public OAuthResult.OAuthResultDto SocialLogin(MemberRequestDto.OAuthRequestDto request,String type) {
        Member member = memberRepository.findByEmail(request.getEmail()).orElse(null);
        if(member != null) {
            String accessToken = null;
            Optional<FcmToken> fcmToken = fcmTokenRepository.findByTokenAndSerialNumber(request.getFcmToken(), request.getSerialNumber());
            if(fcmToken.isEmpty()) {
                FcmToken savedToken = fcmTokenRepository.save(FcmToken.builder()
                        .member(member)
                        .token(request.getFcmToken())
                        .serialNumber(request.getSerialNumber())
                        .build());

                savedToken.setMember(member);
            }
            if (type.equals("kakao"))
                return OAuthResult.OAuthResultDto.builder()
                        .isLogin(true)
                        .memberId(member.getMemberId())
                        .accessToken(redisService.saveLoginStatus(member.getMemberId(),tokenProvider.createAccessToken(member.getMemberId(), SocialType.KAKAO.toString(), request.getEmail(), Arrays.asList(new SimpleGrantedAuthority("USER")))))
                        .refreshToken(redisService.generateRefreshToken(request.getEmail()).getToken())
                        .build();
            else
                return OAuthResult.OAuthResultDto.builder()
                        .isLogin(true)
                        .memberId(member.getMemberId())
                        .accessToken(redisService.saveLoginStatus(member.getMemberId(), tokenProvider.createAccessToken(member.getMemberId(), SocialType.GOOGLE.toString(), request.getEmail(),Arrays.asList(new SimpleGrantedAuthority("USER")))))
                        .refreshToken(redisService.generateRefreshToken(request.getEmail()).getToken())
                        .build();
        }
        return OAuthResult.OAuthResultDto.builder()
                .isLogin(false)
                .build();
    }

    @Override
    public Optional<Member> checkExistNickname(String nickname){
        return memberRepository.findByNickname(nickname);
    }

    @Override
    public void existByPhoneNumber(String phoneNum) {
        if (memberRepository.existsByPhoneNum(phoneNum)) {
            throw new AuthNumberException(Code.PHONE_NUMBER_EXIST);
        }
    }


    @Override
    public void deletePreferCategoryByMember(Member member) {
        preferCategoryRepository.deleteByMember(member);
    }
    @Override
    @Transactional
    public void updateMemberPreferCategory(Member member, MemberRequestDto.changeCategoryDto request) {
        deletePreferCategoryByMember(member);
        for (String categoryName : request.getCategories()) {
            Category category = categoryRepository.findByName(categoryName).get();
            preferCategoryRepository.save(MemberPreferCategory.builder()
                    .member(member)
                    .category(category)
                    .build()
            );
        }
    }



    @Override
    @Transactional
    public String updateMemberProfileImage(Member member, MemberRequestDto.changeProfileDto profileDto) throws IOException {
        Uuid uuid = s3Manager.createUUID();
        String KeyName = s3Manager.generateMemberKeyName(uuid);
        String fileUrl = s3Manager.uploadFile(KeyName, profileDto.getNewProfile());
        member.setProfileUrl(fileUrl);
        return fileUrl;
    }

    @Override
    @Transactional
    public void updateMemberBasicInfo(Member member, MemberResponseDto.MemberBasicInfoDto memberBasicInfoDto) {
        int age = MemberConverter.calculateAge(memberBasicInfoDto.getBirth());
        member.setBasicInfo(age,memberBasicInfoDto);
    }
    @Override
    @Transactional
    public void updateMemberDetailInfo(Member member, MemberResponseDto.MemberDetailInfoDto memberDetailInfoDto) {
        member.setDetailInfo(memberDetailInfoDto);
    }

    @Override
    @Transactional
    public void updateMemberNickname(Member member, String newNickname) {
        member.setNickname(newNickname);
    }

    @Override
    @Transactional
    public void logout(String accessToken, Member member) {
        redisService.resolveLogout(accessToken);
        List<FcmToken> fcmTokenList = fcmTokenRepository.findAllByMember(member);
        fcmTokenList.stream()
                        .map(
                                fcmToken ->
                                {
                                    fcmTokenRepository.delete(fcmToken);
                                    return null;
                                }
                        ).collect(Collectors.toList());
    }

    @Override
    public String regenerateAccessToken(RefreshToken refreshToken) {
        Member member = memberRepository.findById(refreshToken.getMemberId()).orElseThrow(() -> new MemberException(Code.MEMBER_NOT_FOUND));
        return redisService.saveLoginStatus(member.getMemberId(), tokenProvider.createAccessToken(member.getMemberId(), member.getSocialType().toString(),member.getEmail(),Arrays.asList(new SimpleGrantedAuthority("USER"))));
    }

    @Override
    public List<Terms> getAllTerms() {
        return termsRepository.findAll();
    }

    @Override
    public String tempLoginService() {
        return tokenProvider.createTempAccessToken(Arrays.asList(new SimpleGrantedAuthority("GUEST")));
    }

    @Override
    @Transactional(readOnly = false)
    public OAuthJoin.OAuthJoinDto joinInfoComplete(MemberRequestDto.MemberInfoDto request, String type){
        List<Terms> termsList = termsRepository.findByIdIn(request.getAgreeTermsIdList());
        Member joinUser = MemberConverter.toSocialMember(request,type);

        request.getPreferBeverages().stream()
                .map(prefer ->
                        {
                            Category category = categoryRepository.findById(prefer).orElseThrow(() -> new MemberException(Code.NO_CATEGORY_EXIST));
                            MemberPreferCategory memberPreferCategory = MemberConverter.toMemberPreferCategory(joinUser, category);
                            return preferCategoryRepository.save(memberPreferCategory);
                        }
                ).collect(Collectors.toList());

        termsList.stream()
                .map(terms ->
                {
                    TermsAgree savedTermsAgree  = termsAgreeRepository.save(
                            TermsAgree.builder()
                            .terms(terms)
                            .member(joinUser)
                                    .infoAgreeDate(LocalDate.now())
                            .build());
                    savedTermsAgree.setMember(joinUser);
                    savedTermsAgree.setTerms(terms);
                    return savedTermsAgree;
                }).collect(Collectors.toList());

        return OAuthJoin.OAuthJoinDto.builder()
                .refreshToken(redisService.generateRefreshToken(request.getEmail()).getToken())
                .accessToken(redisService.saveLoginStatus(joinUser.getMemberId(), tokenProvider.createAccessToken(joinUser.getMemberId(), type.equals("kakao") ? SocialType.KAKAO.toString() : SocialType.GOOGLE.toString(),request.getEmail(),Arrays.asList(new SimpleGrantedAuthority("USER")))))
                .build();
    }

    @Override
    @Transactional
    public List<Category> findMemberPreferCategories(Member member) {
        List<MemberPreferCategory> categories = preferCategoryRepository.findByMember(member);
        List<Category> categoryList = new ArrayList<>();
        for (MemberPreferCategory memberPreferCategory : categories) {
            categoryList.add(memberPreferCategory.getCategory());
        }
        return categoryList;
    }

    @Override
    @Transactional
    public void memberDeregister(Member member, MemberRequestDto.DeregisterDto request) {
        inactivateMember(member);
        Long deregisterId = saveDeregisterInfo(member.getPhoneNum(), request);
        saveDeregisterReasons(deregisterId,request.getDeregisterTypes());

    }

    @Override
    @Transactional
    public void inactivateMember(Member member) {
        member.inactivateStatus();
    }

    @Override
    @Transactional
    public Long saveDeregisterInfo(String phoneNum, MemberRequestDto.DeregisterDto request) {
        Deregister deregister = MemberConverter.toDeregister(phoneNum, request);
        deregisterRepository.save(deregister);

        for (DeregisterType deregisterType : request.getDeregisterTypes()) {
            deregisterReasonRepository.save(
                    DeregisterReason.builder()
                            .deregister(deregister)
                            .deregisterType(deregisterType)
                            .build());

        }

        return deregister.getId();
    }

    @Override
    @Transactional
    public void saveDeregisterReasons(Long deregisterId, List<DeregisterType> deregisterTypeList) {
//        for (DeregisterType deregisterType : deregisterTypeList) {
//            DeregisterReason.builder()
//                    .
//        }
    }
}

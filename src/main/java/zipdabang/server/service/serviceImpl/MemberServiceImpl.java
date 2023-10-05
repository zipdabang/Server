package zipdabang.server.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.apiPayload.exception.handler.AuthNumberException;
import zipdabang.server.apiPayload.exception.handler.MemberException;
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
import zipdabang.server.repository.memberRepositories.FcmTokenRepository;
import zipdabang.server.repository.memberRepositories.InqueryRepository;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.repository.memberRepositories.PreferCategoryRepository;
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

    private final InqueryRepository inqueryRepository;
    private final AmazonS3Manager s3Manager;
    private final DeregisterRepository deregisterRepository;
    private final DeregisterReasonRepository deregisterReasonRepository;
    private final BlockedMemberRepository blockedMemberRepository;

    private final FollowRepository followRepository;

    @Value("${paging.size}")
    private Integer pageSize;

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
    public Optional<Member> findMemberById(Long id) {
        return memberRepository.findById(id);
    }

    @Override
    public void existByPhoneNumber(String phoneNum) {
        if (memberRepository.existsByPhoneNum(phoneNum)) {
            throw new AuthNumberException(CommonStatus.PHONE_NUMBER_EXIST);
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
        Member member = memberRepository.findById(refreshToken.getMemberId()).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));
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
    @Transactional
    public Inquery createInquery(Member member, MemberRequestDto.InqueryDto request) {
        Inquery inquery = MemberConverter.toInquery(request);
        List<InqueryImage> inqueryImageList = null;
        if(request.getImageList() != null)
            if(request.getImageList().size() > 0) {
                inqueryImageList = MemberConverter.toInqueryImage(request.getImageList());
                inqueryImageList.forEach(inqueryImage -> inqueryImage.setInquery(inquery));
            }
        inquery.setMember(member);
        return inqueryRepository.save(inquery);
    }

    @Override
    public Page<Inquery> findInquery(Member member, Integer page) {
        page -= 1;
        Page<Inquery> inqueries = inqueryRepository.findByMember(member, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        if(inqueries.getTotalPages() <= page)
            throw new MemberException(CommonStatus.OVER_PAGE_INDEX_ERROR);
        return inqueries;
    }

    @Override
    @Transactional(readOnly = false)
    public OAuthJoin.OAuthJoinDto joinInfoComplete(MemberRequestDto.MemberInfoDto request, String type){
        List<Terms> termsList = termsRepository.findByIdIn(request.getAgreeTermsIdList());
        Member joinUser = MemberConverter.toSocialMember(request,type);

        request.getPreferBeverages().stream()
                .map(prefer ->
                        {
                            Category category = categoryRepository.findById(prefer).orElseThrow(() -> new MemberException(CommonStatus.NO_CATEGORY_EXIST));
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

    @Override
    @Transactional
    public void blockMember(Member owner, Long blockedId) {
        if (owner.getMemberId() == blockedId) {
            throw new MemberException(CommonStatus.BLOCK_SELF);
        }
        Member blocked = memberRepository.findById(blockedId).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));
        if (blockedMemberRepository.existsByOwnerAndBlocked(owner, blocked)) {
            throw new MemberException(CommonStatus.ALREADY_BLOCKED_MEMBER);
        }
        blockedMemberRepository.save(
                BlockedMember.builder()
                        .owner(owner)
                        .blocked(blocked)
                        .build());
    }

    @Override
    @Transactional
    public void unblockMember(Member owner, Long blockedId) {
        Member blocked = memberRepository.findById(blockedId).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));
        blockedMemberRepository.deleteByOwnerAndBlocked(owner, blocked);
    }

    @Override
    @Transactional
    public Page<Member> findBlockedMember(Integer page, Member member) {

        Page<Member> blockedMembers = blockedMemberRepository.findBlockedByOwner(member, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        if (blockedMembers.getContent().isEmpty()) {
            throw new MemberException(CommonStatus.BLOCKED_MEMBER_NOT_FOUND);
        }
        if(blockedMembers.getTotalPages() <= page)
            throw new MemberException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return blockedMembers;

    }

    @Override
    @Transactional
    public Follow toggleFollow(Long targetId, Member member) {
        if(targetId.equals(member.getMemberId()))
            throw new MemberException(CommonStatus.SELF_FOLLOW_FORBIDDEN);
        Member target = memberRepository.findById(targetId).get();

        Optional<Follow> checkFollow = followRepository.findByFollowerAndFollowee(member, target);

        // 팔로우 하기
        if (checkFollow.isEmpty()) {
            Follow follow = MemberConverter.toFollow();
            // 내 팔로우 대상에게 팔로워 목록 추가
            follow.setFollower(member);
            // 내 팔로잉 목록에 해당 멤버를 넣기
            follow.setFollowee(target);
            return follow;
        }else{
            // 팔로우 끊기
            checkFollow.get().cancleFollow(target,member);
            followRepository.delete(checkFollow.get());
            return null;
        }
    }

    @Override
    public Page<Follow> findFollowing(Member member, Integer page) {
        page -= 1;
        Page<Follow> followingMember = followRepository.findAllByFollower(member, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        if(followingMember.getTotalPages() <= page)
            throw new MemberException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return followingMember;
    }

    @Override
    public Page<Follow> findFollower(Member member, Integer page) {
        page -= 1;
        Page<Follow> followerMember = followRepository.findAllByFollowee(member, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        if(followerMember.getTotalPages() <= page)
            throw new MemberException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return followerMember;
    }

    @Override
    public Boolean checkFollowing(Member loginMember, Member targetMember) {
        return followRepository.findByFollowerAndFollowee(loginMember,targetMember).isPresent();
    }
}







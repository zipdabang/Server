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
import zipdabang.server.domain.inform.PushAlarm;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.MemberPreferCategory;
import zipdabang.server.domain.member.Terms;
import zipdabang.server.domain.member.TermsAgree;
import zipdabang.server.domain.member.*;
import zipdabang.server.redis.domain.RefreshToken;
import zipdabang.server.repository.AlarmRepository.PushAlarmRepository;
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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static AmazonS3Manager staticAmazonS3Manager;
    private final DeregisterRepository deregisterRepository;
    private final DeregisterReasonRepository deregisterReasonRepository;
    private final BlockedMemberRepository blockedMemberRepository;
    private final FollowRepository followRepository;

    private final PushAlarmRepository pushAlarmRepository;

    private final MemberReportRepository memberReportRepository;

    private static String defaultProfileImage;

    @Value("${paging.size}")
    private Integer pageSize;

    @Value("${cloud.aws.s3.user-default-image}")
    public void setDefaultImage(String value) {
        defaultProfileImage = value;
    }
    @PostConstruct
    public void init(){
        this.staticAmazonS3Manager = this.s3Manager;}

    @Override
    @Transactional
    public OAuthResult.OAuthResultDto SocialLogin(MemberRequestDto.OAuthRequestDto request,String type) {
        SocialType socialType = SocialType.valueOf(type.toUpperCase());
        Member member = memberRepository.findByEmailAndSocialType(request.getEmail(),socialType).orElse(null);

        log.info("the data from frontend : {}",request.toString());
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
                        .refreshToken(redisService.generateRefreshToken(request.getEmail(),socialType).getToken())
                        .build();
            else
                return OAuthResult.OAuthResultDto.builder()
                        .isLogin(true)
                        .memberId(member.getMemberId())
                        .accessToken(redisService.saveLoginStatus(member.getMemberId(), tokenProvider.createAccessToken(member.getMemberId(), SocialType.GOOGLE.toString(), request.getEmail(),Arrays.asList(new SimpleGrantedAuthority("USER")))))
                        .refreshToken(redisService.generateRefreshToken(request.getEmail(),socialType).getToken())
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
        if (!member.getProfileUrl().equals(defaultProfileImage)) {
            s3Manager.deleteFile(toKeyName(member.getProfileUrl()).substring(1));
        }
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
    public void joinDeregisterCheck(String email, String phoneNum, SocialType socialType) {
        if (deregisterRepository.existsByEmailAndSocialTypeAndPassedSevenDays(email, socialType,false)) {
            log.info("탈퇴한 지 1주일이 지나지 않은 이메일&소셜타입 : " + email + "," + socialType);
            throw new MemberException(CommonStatus.NOT_PASSED_SEVEN_DAYS_EMAIL);
        } else if (deregisterRepository.existsByPhoneNumAndPassedSevenDays(phoneNum,false)) {
            log.info("탈퇴한 지 1주일이 지나지 않은 전화번호 : " + phoneNum);
            throw new MemberException(CommonStatus.NOT_PASSED_SEVEN_DAYS_PHONE_NUM);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public OAuthJoin.OAuthJoinDto joinInfoComplete(MemberRequestDto.MemberInfoDto request, String type){
        if (memberRepository.existsByEmailAndSocialType(request.getEmail(), SocialType.valueOf(type.toUpperCase()))) {
            throw new MemberException(CommonStatus.EXIST_EMAIL_AND_SOCIAL_TYPE);
        } else if (memberRepository.existsByPhoneNum(request.getPhoneNum())) {
            throw new MemberException(CommonStatus.EXIST_PHONE_NUMBER);
        }
        List<Terms> termsList = termsRepository.findByIdIn(request.getAgreeTermsIdList());
        Member joinUser = MemberConverter.toSocialMember(request,type);
        memberRepository.save(joinUser);

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

        FcmToken fcmToken = FcmToken.builder()
                .token(request.getFcmToken())
                .serialNumber(request.getSerialNumber())
                .build();

        fcmToken.setMember(joinUser);

        fcmTokenRepository.save(fcmToken);

        return OAuthJoin.OAuthJoinDto.builder()
                .refreshToken(redisService.generateRefreshToken(request.getEmail(), SocialType.valueOf(type.toUpperCase())).getToken())
                .accessToken(redisService.saveLoginStatus(joinUser.getMemberId(), tokenProvider.createAccessToken(joinUser.getMemberId(), type.equals("kakao") ? SocialType.KAKAO.toString() : SocialType.GOOGLE.toString(), request.getEmail(), Arrays.asList(new SimpleGrantedAuthority("USER")))))
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

    }

    @Override
    @Transactional
    public void inactivateMember(Member member) {
        member.inactivateStatus();
    }

    @Override
    @Transactional
    public Long saveDeregisterInfo(Member member, MemberRequestDto.DeregisterDto request) {
        Deregister deregister = MemberConverter.toDeregister(member.getPhoneNum(),member.getEmail(),member.getSocialType(), request.getFeedback());
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
    public Long getFollowingCount(Member member) {
        return followRepository.countByFollower(member);
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
    public Long getFollowerCount(Member member) {
        return followRepository.countByFollowee(member);
    }


    @Override
    @Transactional
    public void updateCaption(Member member, MemberRequestDto.changeCaptionDto captionDto) {
        member.setCaption(captionDto.getCaption());

    }

    public static String toKeyName(String imageUrl) {
        String input = imageUrl;

        Pattern regex = Pattern.compile(staticAmazonS3Manager.getPattern());
        Matcher matcher = regex.matcher(input);
        String extractedString = null;
        if (matcher.find())
            extractedString = matcher.group(1);

        return extractedString;

    }

    @Override
    @Transactional
    public void updateProfileDefault(Member member) {
        if (member.getProfileUrl().equals(defaultProfileImage)) {
            return;
        }
        s3Manager.deleteFile(toKeyName(member.getProfileUrl()).substring(1));
        member.setProfileUrl(defaultProfileImage);
    }

    @Override
    public Boolean checkFollowing(Member loginMember, Member targetMember) {
        return followRepository.findByFollowerAndFollowee(loginMember,targetMember).isPresent();
    }

    @Override
    public MemberResponseDto.MyZipdabangDto getMyZipdabang(Member member, Long targetId) {
        Member target = memberRepository.findById(targetId).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));
        boolean checkSelf = false;
        if (member.getMemberId() == target.getMemberId()) {
            checkSelf=true;
        }
        else if(blockedMemberRepository.existsByOwnerAndBlocked(member,target)){
            throw new MemberException(CommonStatus.BLOCKED_MEMBER);
        }
        boolean isFollowing = followRepository.existsByFollowerAndFollowee(member, target);
        boolean isFollower = followRepository.existsByFollowerAndFollowee(target, member);

        List<Category> categories = findMemberPreferCategories(target);
        MemberResponseDto.MemberPreferCategoryDto memberPreferCategoryDto = MemberConverter.toMemberPreferCategoryDto(categories);

        return MemberConverter.toMyZipdabangDto(target, checkSelf, isFollowing, isFollower, memberPreferCategoryDto);
    }

    @Override
    public MemberResponseDto.MyZipdabangDto getSelfMyZipdabang(Member member) {
        boolean checkSelf = true;
        boolean isFollowing = false;
        boolean isFollower = false;

        List<Category> categories = findMemberPreferCategories(member);
        MemberResponseDto.MemberPreferCategoryDto memberPreferCategoryDto = MemberConverter.toMemberPreferCategoryDto(categories);

        return MemberConverter.toMyZipdabangDto(member, checkSelf, isFollowing, isFollower, memberPreferCategoryDto);
    }

    @Override
    public Page<PushAlarm> getPushAlarms(Member member, Integer page) {
        page -= 1;

        Page<PushAlarm> pushAlarms = pushAlarmRepository.findByOwnerMember(member, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        if(pushAlarms.getTotalPages() <= page)
            throw new MemberException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return pushAlarms;
    }

    @Override
    public Page<Member> findByNicknameContains(Integer page, String nickname) {
        Page<Member> searchByNicknameMembers = memberRepository.findByNicknameContains(nickname, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        if (searchByNicknameMembers.getContent().isEmpty()) {
            throw new MemberException(CommonStatus.NICKNAME_MEMBER_NOT_EXIST);
        }
        if(searchByNicknameMembers.getTotalPages() <= page)
            throw new MemberException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return searchByNicknameMembers;
    }

    @Override
    public Page<Member> findFollowerByNicknameContains(Integer page, Long targetId, String nickname) {
        Member member = memberRepository.findById(targetId).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));
        Page<Member> searchByNicknameMembers = memberRepository.qFindFollowerByNicknameContains(nickname, member, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        if (searchByNicknameMembers.getContent().isEmpty()) {
            throw new MemberException(CommonStatus.NICKNAME_MEMBER_NOT_EXIST);
        }
        if(searchByNicknameMembers.getTotalPages() <= page)
            throw new MemberException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return searchByNicknameMembers;
    }

    @Override
    public Page<Member> findFollowingByNicknameContains(Integer page, Long targetId, String nickname) {
        Member member = memberRepository.findById(targetId).orElseThrow(() -> new MemberException(CommonStatus.MEMBER_NOT_FOUND));
        Page<Member> searchByNicknameMembers = memberRepository.qFindFollowingByNicknameContains(nickname, member, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        if (searchByNicknameMembers.getContent().isEmpty()) {
            throw new MemberException(CommonStatus.NICKNAME_MEMBER_NOT_EXIST);
        }
        if(searchByNicknameMembers.getTotalPages() <= page)
            throw new MemberException(CommonStatus.OVER_PAGE_INDEX_ERROR);

        return searchByNicknameMembers;
    }


    @Override
    public Optional<Inquery> findInqueryById(Long inqueryId) {
        return inqueryRepository.findById(inqueryId);
    }

    @Override
    public Inquery findMyInqueryById(Member member,Long inqueryId) {

        Inquery inquery = inqueryRepository.findById(inqueryId).get();

        if(!Objects.equals(inquery.getMember().getMemberId(), member.getMemberId()))
            throw new MemberException(CommonStatus.NOT_MY_INQUERY);

        return inquery;
    }

    @Override
    @Transactional
    public MemberReport reportMember(Member member, Long targetId) {

        Member targetMember = memberRepository.findById(targetId).orElseThrow(()->new MemberException(CommonStatus.MEMBER_NOT_FOUND));

        return memberReportRepository.save(MemberReport.builder()
                .reporter(member)
                .reported(targetMember)
                .build());
    }
}
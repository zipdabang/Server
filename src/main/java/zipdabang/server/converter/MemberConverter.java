package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.aws.s3.AmazonS3Manager;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.MemberException;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.enums.GenderType;
import zipdabang.server.domain.enums.SocialType;
import zipdabang.server.domain.etc.Uuid;
import zipdabang.server.domain.member.*;
import zipdabang.server.domain.member.Deregister;
import zipdabang.server.domain.member.Terms;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.member.MemberPreferCategory;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.utils.converter.TimeConverter;
import zipdabang.server.utils.dto.OAuthJoin;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemberConverter {

    private final MemberRepository memberRepository;

    private static MemberRepository staticMemberRepository;

    private final AmazonS3Manager amazonS3Manager;

    private static String defaultProfileImage;

    private static AmazonS3Manager staticAmazonS3Manager;

    @Value("${cloud.aws.s3.user-default-image}")
    public void setDefaultImage(String value) {
        defaultProfileImage=value;
    }

    public static MemberResponseDto.JoinMemberDto toJoinMemberDto(Member member){
            return MemberResponseDto.JoinMemberDto.builder()
                    .memberId(member.getMemberId())
                    .nickname(member.getNickname()).build();
//                .accessToken()
    }

    public static int calculateAge(String birth) {
        String year = birth.substring(0,2);

        // 현재 날짜를 가져옴
        LocalDate currentDate = LocalDate.now();

        // 생년월일과 현재 날짜를 기준으로 만 나이 계산
        int age = currentDate.getYear() % 100 - Integer.valueOf(year)- 1;

        if (age < 0)
            age += 100;

        // 현재 연도를 가져옴
        int currentYear = LocalDate.now().getYear();

        // 날짜 문자열을 LocalDate로 변환
        LocalDate date = LocalDate.parse(birth, DateTimeFormatter.ofPattern("yyMMdd"));

        // 생년월일에 현재 연도를 설정하여 완전한 날짜로 만듦
        LocalDate completeDate = date.withYear(currentYear);

        age = ChronoUnit.DAYS.between(completeDate, currentDate) >= 0 ? age + 1 : age;
        return age;
    }



    public static Member toSocialMember(MemberRequestDto.MemberInfoDto request, String type) {
        int age = calculateAge(request.getBirth());
        GenderType gender = Integer.valueOf(request.getGender()) % 2 == 0 ? GenderType.WOMAN : GenderType.MAN;


        Member member = Member.builder()
                .age(age)
                .profileUrl(defaultProfileImage)
                .socialType(type.equals("kakao") ? SocialType.KAKAO : SocialType.GOOGLE)
                .email(request.getEmail())
                .nickname(request.getNickname())
                .birth(request.getBirth())
                .gender(gender)
                .name(request.getName())
                .phoneNum(request.getPhoneNum())
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
        this.staticAmazonS3Manager = amazonS3Manager;
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

    public static MemberResponseDto.MemberBasicInfoDto memberBasicInfoDto(Member member) {
        return MemberResponseDto.MemberBasicInfoDto.builder()
                .name(member.getName())
                .birth(member.getBirth())
                .genderType(member.getGender())
                .phoneNum(member.getPhoneNum())
                .build();
    }

    public static MemberResponseDto.MemberDetailInfoDto memberDetailInfoDto(Member member) {
        return MemberResponseDto.MemberDetailInfoDto.builder()
                .zipCode(member.getZipCode())
                .address(member.getAddress())
                .detailAddress(member.getDetailAddress())
                .build();
    }

    public static MemberResponseDto.MemberInfoResponseDto toMemberInfoDto(Member member,MemberResponseDto.MemberPreferCategoryDto preferCategories) {
        return MemberResponseDto.MemberInfoResponseDto.builder()
                .profileUrl(member.getProfileUrl())
                .email(member.getEmail())
                .memberBasicInfoDto(memberBasicInfoDto(member))
                .memberDetailInfoDto(memberDetailInfoDto(member))
                .nickname(member.getNickname())
                .preferCategories(preferCategories)
                .build();
    }

    public static Member toMember(Long memberId){
        return staticMemberRepository.findById(memberId).orElseThrow(()->new MemberException(Code.MEMBER_NOT_FOUND));
    }

    public static Member toMemberTemp(Long memberId){
        return Member.builder()
                .memberId(memberId)
                .build();
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

    public static MemberResponseDto.CategoryDto toMemberPreferCategoryDto(Category category) {
        return MemberResponseDto.CategoryDto.builder()
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .build();
    }

    public static MemberResponseDto.MemberPreferCategoryDto toMemberPreferCategoryDto(List<Category> categories) {
        List<MemberResponseDto.CategoryDto> categoryDtoList = categories.stream()
                .map(category -> toMemberPreferCategoryDto(category)).collect(Collectors.toList());

        return MemberResponseDto.MemberPreferCategoryDto.builder()
                .categories(categoryDtoList)
                .size(categories.size())
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

    public static MemberResponseDto.TempLoginDto toTempLoginDto(String token){
        return MemberResponseDto.TempLoginDto.builder()
                .accessToken(token)
                .build();
    }

    public static MemberResponseDto.MemberInqueryResultDto toMemberInqueryResultDto(Inquery inquery){
        return MemberResponseDto.MemberInqueryResultDto.builder()
                .id(inquery.getId())
                .created_at(inquery.getMember().getCreatedAt())
                .build();
    }


    public static List<InqueryImage> toInqueryImage(List<MultipartFile> imageList)
    {
        List<InqueryImage> inqueryImageList = imageList.stream()
                .map(
                        image ->
                        {
                            try {
                                Uuid uuid = staticAmazonS3Manager.createUUID();
                                String keyName = staticAmazonS3Manager.generateInqueryKeyName(uuid);
                                String inqueryImageUrl = staticAmazonS3Manager.uploadFile(keyName, image);
                                return InqueryImage.builder()
                                        .imageUrl(inqueryImageUrl)
                                        .build();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                ).collect(Collectors.toList());
        return inqueryImageList;
    }

    public static Inquery toInquery(MemberRequestDto.InqueryDto request){
        return Inquery.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .receiveEmail(request.getEmail())
                .inqueryImageList(new ArrayList<>())
                .build();
    }

    public static MemberResponseDto.InqueryPreviewDto toInqueryPreviewDto(Inquery inquery){
        return MemberResponseDto.InqueryPreviewDto.builder()
                .id(inquery.getId())
                .title(inquery.getTitle())
                .createdAt(TimeConverter.ConvertTime(inquery.getCreatedAt()))
                .build();
    }

    public static MemberResponseDto.InqueryListDto toInqueryListDto(Page<Inquery> inqueryPage){

        List<MemberResponseDto.InqueryPreviewDto> inqueryPreviewDtoList = inqueryPage.getContent().stream()
                .map(MemberConverter::toInqueryPreviewDto).collect(Collectors.toList());

        return MemberResponseDto.InqueryListDto.builder()
                .inqueryList(inqueryPreviewDtoList)
                .isFirst(inqueryPage.isFirst())
                .isLast(inqueryPage.isLast())
                .currentPageElements(inqueryPage.getNumberOfElements())
                .totalElements(inqueryPage.getTotalElements())
                .totalPage(inqueryPage.getTotalPages())

    public static Deregister toDeregister(String phoneNum, MemberRequestDto.DeregisterDto request) {
        return Deregister.builder()
                .phoneNum(phoneNum)
                .feedback(request.getFeedback())
                .build();
    }
}

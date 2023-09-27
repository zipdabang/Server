package zipdabang.server.web.dto.responseDto;

import lombok.*;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.enums.GenderType;


import java.time.LocalDateTime;
import java.util.List;

public class MemberResponseDto {


    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class JoinMemberDto {
        private Long memberId;
        private String nickname;
        private String accessToken;
    }
    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SocialJoinDto {
        private String accessToken;
        private String refreshToken;
    }


    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SocialLoginDto{
        private String accessToken;
        private String refreshToken;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberProfileDto {

        private String nickname;
        private String email;
        private String phoneNum;
        private String profileUrl;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberBasicInfoDto{
        private String name;
        private String birth;
        private GenderType genderType;
        private String phoneNum;
    }
    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberDetailInfoDto{
        private String zipCode;
        private String address;
        private String detailAddress;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberInfoResponseDto {
        private String profileUrl;
        private String email;
        private MemberBasicInfoDto memberBasicInfoDto;
        private MemberDetailInfoDto memberDetailInfoDto;
        private String nickname;
        private MemberPreferCategoryDto preferCategories;

    }


    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberStatusDto{
        private Long memberId;
        private String status;
        private LocalDateTime calledAt;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class IssueNewTokenDto{
        private String refreshToken;
        private String accessToken;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TermsDto{
        Long termsId;
        String termsTitle;
        String termsBody;
        Boolean isMoreToSee;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TermsListDto{
        List<TermsDto> termsList;
        Integer size;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TempLoginDto{
        String accessToken;
    }


    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CategoryDto{
        private String name;

        private String imageUrl;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberPreferCategoryDto{
        List<CategoryDto> categories;
        Integer size;
    }


    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberInqueryResultDto{
        private Long id;
        private LocalDateTime created_at;
    }
}

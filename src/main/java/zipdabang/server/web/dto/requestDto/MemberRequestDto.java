package zipdabang.server.web.dto.requestDto;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.domain.enums.DeregisterType;
import zipdabang.server.validation.annotation.CheckNickname;
import zipdabang.server.validation.annotation.ExistNickname;

public class MemberRequestDto {

    @Getter
    @Setter
    public static class quitMember {
        @Override
        public String toString() {
            return "quitMember{" +
                    "memberId=" + memberId +
                    ", nickname='" + nickname + '\'' +
                    ", quitDate=" + quitDate +
                    ", status=" + status +
                    '}';
        }

        private Long memberId;
        private String nickname;
        private String quitDate;
        private String status;
    }

    @Getter
    @Setter
    public static class restoreMember {
        @Override
        public String toString() {
            return "quitMember{" +
                    "memberId=" + memberId +
                    ", nickname='" + nickname + '\'' +
                    ", restoreDate=" + restoreDate +
                    ", status=" + status +
                    '}';
        }

        private Long memberId;
        private String nickname;
        private String restoreDate;
        private String status;
    }

    @Getter
    @Setter
    public static class OAuthRequestDto {
        @Override
        public String toString() {
            return "OAuthRequestDto{" +
                    "email='" + email + '\'' +
                    ", fcmToken='" + fcmToken + '\'' +
                    ", serialNumber='" + serialNumber + '\'' +
                    '}';
        }
        private String email;
        private String fcmToken;
        private String serialNumber;
    }


    @Getter
    @Setter
    public static class MemberInfoDto {

        @NotBlank
        private String name;
        @NotBlank @Pattern(regexp = "[0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[1,2][0-9]|3[0,1])", message = "생년월일 형식과 맞지 않습니다.")

        private String birth;
        @NotBlank @Pattern(regexp = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$", message = "이메일 형식과 맞지 않습니다.")
        private String email;
        @NotBlank
        private String gender;
        @NotBlank @ExistNickname @CheckNickname @Size(min = 2, max = 6)
        private String nickname;
        @NotBlank @Pattern(regexp = "01[0-9]{8,9}", message = "전화번호 형식과 맞지 않습니다.")

        private String phoneNum;

        @NotBlank
        private String fcmToken;

        @NotBlank
        private String serialNumber;

        private List<Long> agreeTermsIdList;

        private List<Long> preferBeverages;

        @Override
        public String toString() {
            return "MemberInfoDto{" +
                    "name='" + name + '\'' +
                    ", birth='" + birth + '\'' +
                    ", email='" + email + '\'' +
                    ", gender='" + gender + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", phoneNum='" + phoneNum + '\'' +
                    ", fcmToken='" + fcmToken + '\'' +
                    ", serialNumber='" + serialNumber + '\'' +
                    ", agreeTermsIdList=" + agreeTermsIdList +
                    ", preferBeverages=" + preferBeverages +
                    '}';
        }
    }

    @Getter
    @Setter
    public static class SmsRequestDto {
        @Override
        public String toString() {
            return "SmsRequestDto{" +
                    "targetPhoneNum='" + targetPhoneNum + '\'' +
                    '}';
        }

        private String targetPhoneNum;
    }

    @Getter
    @Setter
    public static class PhoneNumAuthDto {
        @Override
        public String toString() {
            return "PhoneNumAuthDto{" +
                    "phoneNum='" + phoneNum + '\'' +
                    ", authNum=" + authNum +
                    '}';
        }

        private String phoneNum;
        private Integer authNum;
    }

    @Getter
    @Setter
    public static class memberProfileDto {

        @Override
        public String toString() {
            return "memberProfileDto{" +
                    "name='" + name + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", email='" + email + '\'' +
                    ", phoneNum='" + phoneNum + '\'' +
                    ", profileImage=" + profileImage +
                    '}';
        }

        private String name;
        private String nickname;
        private String email;
        private String phoneNum;
        private MultipartFile profileImage;
    }

    @Getter
    @Setter
    public static class changeCategoryDto {
        List<String> categories;
    }

    @Getter
    @Setter
    public static class changeNicknameDto {
        @CheckNickname @ExistNickname
        String nickname;
    }

    @Getter
    @Setter
    public static class changeCaptionDto {
        String caption;
    }

    @Getter
    @Setter
    public static class changeProfileDto {
        private MultipartFile newProfile;
    }

    @Getter
    @Setter
    public static class IssueTokenDto {
        String refreshToken;
    }

    @Getter
    @Setter
    public static class InqueryDto {

        @NotBlank
        String email;

        @NotBlank @Size(max = 20)
        String title;

        @NotBlank @Size(max = 500)
        String body;

        @Nullable
        List<MultipartFile> imageList;
    }

    @Getter
    @Setter
    public static class DeregisterDto {
        @Enumerated(EnumType.STRING)
        private List<DeregisterType> deregisterTypes;
        private String feedback;
    }

}

package zipdabang.server.web.dto.requestDto;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.web.dto.responseDto.MemberResponseDto;

public class MemberRequestDto {

    @Getter @Setter
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

    @Getter @Setter
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
        private String email;
        private String fcmToken;
        private String serialNumber;
    }


    @Getter
    @Setter
    public static class MemberInfoDto{

        @NotBlank
        private String name;
        @NotBlank
        private String birth;
        @NotBlank
        private String email;
        @NotBlank
        private String gender;
        @NotBlank
        private String nickname;
        @NotBlank
        private String phoneNum;

        private List<Long> agreeTermsIdList;

        private List<Long> preferBeverages;

    }

    @Getter
    @Setter
    public static class SmsRequestDto{
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

    @Getter @Setter
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

    @Getter @Setter
    public static class changeNicknameDto{
        String nickname;
    }

    @Getter @Setter
    public static class changeProfileDto {
        private MultipartFile newProfile;
    }

    @Getter @Setter
    public static class IssueTokenDto{
        String refreshToken;
    }

    @Getter @Setter
    public static class InqueryDto{

        @NotBlank
        String email;

        @NotBlank @Size(max = 20)
        String title;

        @NotBlank @Size(max = 500)
        String body;

        @Nullable
        List<MultipartFile> imageList;
    }

}

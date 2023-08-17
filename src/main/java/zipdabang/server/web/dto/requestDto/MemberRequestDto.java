package zipdabang.server.web.dto.requestDto;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

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
        private String profileUrl;
        @NotBlank
        private String gender;
        @NotBlank
        private String zipCode;
        @NotBlank
        private String address;
        @NotBlank
        private String detailAddress;
        @NotBlank
        private String nickname;
        @NotBlank
        private String phoneNum;

        private Boolean infoAgree;
        private Boolean infoOthersAgree;

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

    @Getter @Setter
    public static class IssueTokenDto{
        String refreshToken;
    }
}

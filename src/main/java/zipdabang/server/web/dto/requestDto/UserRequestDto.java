package zipdabang.server.web.dto.requestDto;

import lombok.*;

public class UserRequestDto {

    @Getter @Setter
    public static class logoutUser{
        @Override
        public String toString() {
            return "logoutUser{" +
                    "userId=" + userId +
                    ", nickname='" + nickname + '\'' +
                    '}';
        }

        private Long userId;
        private String nickname;
    }

    @Getter @Setter
    public static class quitUser {
        @Override
        public String toString() {
            return "quitUser{" +
                    "userId=" + userId +
                    ", nickname='" + nickname + '\'' +
                    ", quitDate=" + quitDate +
                    ", status=" + status +
                    '}';
        }

        private Long userId;
        private String nickname;
        private String quitDate;
        private String status;
    }

    @Getter @Setter
    public static class restoreUser {
        @Override
        public String toString() {
            return "quitUser{" +
                    "userId=" + userId +
                    ", nickname='" + nickname + '\'' +
                    ", restoreDate=" + restoreDate +
                    ", status=" + status +
                    '}';
        }

        private Long userId;
        private String nickname;
        private String restoreDate;
        private String status;
    }
=======
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class UserRequestDto {

    @Getter
    @Setter
    public static class KakaoSocialDto {
        @Override
        public String toString() {
            return "KakaoSocialDto{" +
                    "socialId='" + socialId + '\'' +
                    "}";
        }

        private String socialId;
    }

    @Getter
    @Setter
    public static class AppleSocialDto{
        @Override
        public String toString() {
            return "AppleSocialDto{" +
                    "identityToken='" + identityToken + '\'' +
                    "}";
        }

        private String identityToken;
    }

    @Getter
    @Setter
    public static class UserInfoDto{


        @NotBlank
        private String name;
        @NotBlank
        private String birth;
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

        private List<Integer> preferBeverages;

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


}
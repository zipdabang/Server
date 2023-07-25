package zipdabang.server.web.dto.requestDto;

import lombok.Getter;
import lombok.Setter;

public class UserRequestDto {

    @Getter @Setter
    public static class userProfileDto {

        @Override
        public String toString(){
            return "userProfileDto{" +
                    "name='" + name + '\'' +
                    "nickname='" + nickname + '\'' +
                    ", email='" + email + '\'' +
                    ", phoneNum=" + phoneNum +
                    ", profileUrl" + profileUrl +
                    '}';
        }

        private String name;
        private String nickname;
        private String email;
        private String phoneNum;
        private String profileUrl;
    }
}

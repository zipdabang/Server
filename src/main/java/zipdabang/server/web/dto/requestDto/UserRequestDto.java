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
}

package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.domain.Users;
import zipdabang.server.web.dto.responseDto.UserResponseDto;

@Component
@RequiredArgsConstructor
public class UserConverter {

    public static UserResponseDto.JoinUserDto toJoinUserDto(Users user){
            return UserResponseDto.JoinUserDto.builder()
                    .userId(user.getUserId())
                    .nickname(user.getNickname()).build();
//                .accessToken()
        }
    public static UserResponseDto.UserProfileDto toUserProfileDto(Users user) {
        return UserResponseDto.UserProfileDto.builder()
//                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNum(user.getPhoneNum())
                .profileUrl(user.getProfileUrl())
                .build();
    }
}

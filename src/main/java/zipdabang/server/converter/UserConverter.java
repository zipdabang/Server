package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.domain.Users;
import zipdabang.server.web.dto.responseDto.UserResponseDto;

@Component
@RequiredArgsConstructor
public class UserConverter {

    public static UserResponseDto.JoinUserDto toJoinUserDto(Users user) {
        return UserResponseDto.JoinUserDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
//                .accessToken()
                .build();
    }
}

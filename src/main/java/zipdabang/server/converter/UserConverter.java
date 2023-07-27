package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.domain.Users;
import zipdabang.server.web.dto.responseDto.UserResponseDto;

@Component
@RequiredArgsConstructor
public class UserConverter {

    public static UserResponseDto.UserIdDto toUserIdDto(Users user) {
        return UserResponseDto.UserIdDto.builder()
                .userId(user.getUserId())
                .build();
    }
}

package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.UserException;
import zipdabang.server.domain.Users;
import zipdabang.server.repository.userRepositories.UsersRepository;
import zipdabang.server.web.dto.responseDto.UserResponseDto;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class UserConverter {

    private final UsersRepository usersRepository;

    private static UsersRepository staticUsersRepository;

    public static UserResponseDto.JoinUserDto toJoinUserDto(Users user){
            return UserResponseDto.JoinUserDto.builder()
                    .userId(user.getUserId())
                    .nickname(user.getNickname()).build();
//                .accessToken()
        }

    @PostConstruct
    public void init() {
        this.staticUsersRepository = this.usersRepository;
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

    public static Users toUser(Long userId){
        return staticUsersRepository.findById(userId).orElseThrow(()->new UserException(Code.MEMBER_NOT_FOUND));
    }
}

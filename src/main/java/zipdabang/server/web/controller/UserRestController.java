package zipdabang.server.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.web.dto.requestDto.UserRequestDto;
import zipdabang.server.web.dto.responseDto.UserResponseDto;

@RestController
@Validated
@RequiredArgsConstructor
public class UserRestController {

    //프로필 수정
    @PatchMapping("/users/{userId}")
    public ResponseDto<UserResponseDto.UserProfileDto> updateProfile(
            @PathVariable("userId") Long userId,
            @RequestBody UserRequestDto.userProfileDto request) {
        return null;
    }

    //프로필 조회
    @GetMapping("/users/{userId}")
    public ResponseDto<UserResponseDto.UserProfileDto> showProfile(@PathVariable("userId") Long userId){
        return null;
    }

    //닉네임 중복검사
    @GetMapping("/users/exist-nickname")
    public ResponseDto<String> checkExistNickname(@RequestParam String nickname) {
        return null;
    }
}

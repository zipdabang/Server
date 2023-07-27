package zipdabang.server.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zipdabang.server.base.ResponseDto;
import zipdabang.server.web.dto.requestDto.UserRequestDto;
import zipdabang.server.web.dto.responseDto.UserResponseDto;

@RestController
@Validated
@RequiredArgsConstructor
public class UserRestController {

    @PostMapping("/users/logout")
    public ResponseDto<UserResponseDto.UserIdDto> logout(@RequestBody UserRequestDto.logoutUser request){
        return null;
    }

    @PatchMapping("/users/quit")
    public ResponseDto<UserResponseDto.UserIdDto> quit(@RequestBody UserRequestDto.quitUser request) {
        return null;
    }

    @PatchMapping("/users/restore")
    public ResponseDto<UserResponseDto.UserIdDto> restore(@RequestBody UserRequestDto.restoreUser request) {
        return null;
    }
}

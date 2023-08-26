package zipdabang.server.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import zipdabang.server.base.exception.GeneralException;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public enum Code {
    OK(HttpStatus.OK,2000, "Ok"),

    OAUTH_LOGIN(HttpStatus.OK,2010, "로그인 입니다."),
    OAUTH_JOIN(HttpStatus.OK,2011,"회원가입 입니다."),
    NICKNAME_EXIST(HttpStatus.OK,2010, "닉네임이 이미 존재합니다."),
    NICKNAME_OK(HttpStatus.OK,2011, "사용 가능한 닉네임 입니다."),
    PHONE_NUMBER_EXIST(HttpStatus.OK, 2020, "이미 인증된 전화번호입니다."),




    // marget response

    WATCHED_NOT_FOUND(HttpStatus.OK, 2060, "조회했던 아이템이 없습니다."),

    //recipe response
    RECIPE_NOT_FOUND(HttpStatus.OK, 2100, "검색어와 일치하는 레시피가 없습니다"),

    // error Codes

    JWT_FORBIDDEN(HttpStatus.FORBIDDEN, 4002, "이미 로그아웃 된 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 4003, "접근 권한이 없습니다."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST,4005 ,"잘못된 요청 입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4006, "UnAuthorized"),
    JWT_BAD_REQUEST(HttpStatus.UNAUTHORIZED, 4006,"잘못된 JWT 서명입니다."),
    JWT_ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 4007,"액세스 토큰이 만료되었습니다."),
    JWT_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 4008,"리프레시 토큰이 만료되었습니다. 다시 로그인하시기 바랍니다."),
    JWT_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, 4009,"지원하지 않는 JWT 토큰입니다."),
    JWT_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 4010,"유효한 JWT 토큰이 없습니다."),

    FEIGN_CLIENT_ERROR_400(HttpStatus.BAD_REQUEST, 4011, "feign에서 400번대 에러가 발생했습니다."),
    MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, 4013,"해당 사용자가 존재하지 않습니다"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, 4014,"refresh token이 필요합니다."),
    LOGOUT_FAIL(HttpStatus.BAD_REQUEST, 4015, "fcmToken, serialNumber 주세요"),

    NO_CATEGORY_EXIST(HttpStatus.BAD_REQUEST, 4017, "선호하는 음료 카테고리가 잘못 되었습니다."),

    PAGE_INDEX_ERROR(HttpStatus.BAD_REQUEST, 4018, "페이지 번호가 이상합니다."),



    // market error

    //recipe error
    NULL_RECIPE_ERROR(HttpStatus.BAD_REQUEST, 4100, "레시피 작성시 누락된 내용이 있습니다."),
    NO_RECIPE_EXIST(HttpStatus.BAD_REQUEST, 4101, "해당 레시피가 존재하지 않습니다."),

    // SMS error
    PHONE_AUTH_NOT_FOUND(HttpStatus.BAD_REQUEST, 4200, "인증 번호 요청이 필요합니다."),
    PHONE_AUTH_ERROR(HttpStatus.BAD_REQUEST, 4201, "잘못된 인증 번호 입니다."),
    PHONE_AUTH_TIMEOUT(HttpStatus.BAD_REQUEST, 4201, "인증 시간이 초과되었습니다."),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "Internal server Error"),
    FEIGN_CLIENT_ERROR_500(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "Inter server Error in feign client");



    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    public String getMessage(Throwable e) {
        return this.getMessage(this.getMessage() + " - " + e.getMessage());
        // 결과 예시 - "Validation error - Reason why it isn't valid"
    }

    public String getMessage(String message) {
        return Optional.ofNullable(message)
                .filter(Predicate.not(String::isBlank))
                .orElse(this.getMessage());
    }

    public static Code valueOf(HttpStatus httpStatus) {
        if (httpStatus == null) {
            throw new GeneralException("HttpStatus is null.");
        }

        return Arrays.stream(values())
                .filter(errorCode -> errorCode.getHttpStatus() == httpStatus)
                .findFirst()
                .orElseGet(() -> {
                    if (httpStatus.is4xxClientError()) {
                        return Code.BAD_REQUEST;
                    } else if (httpStatus.is5xxServerError()) {
                        return Code.INTERNAL_ERROR;
                    } else {
                        return Code.OK;
                    }
                });
    }
}

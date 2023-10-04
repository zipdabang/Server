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

    OAUTH_LOGIN(HttpStatus.OK,2050, "로그인 입니다."),
    OAUTH_JOIN(HttpStatus.OK,2051,"회원가입 입니다."),
    NICKNAME_EXIST(HttpStatus.OK,2052, "닉네임이 이미 존재합니다."),
    NICKNAME_OK(HttpStatus.OK,2053, "사용 가능한 닉네임 입니다."),
    PHONE_NUMBER_EXIST(HttpStatus.OK, 2054, "이미 인증된 전화번호입니다."),
    AUTO_LOGIN_MAIN(HttpStatus.OK, 2055, "홈 화면으로 이동하세요"),
    AUTO_LOGIN_NOT_MAIN(HttpStatus.OK, 2056, "로그인 화면으로 이동하세요"),
    BLOCKED_MEMBER_NOT_FOUND(HttpStatus.OK, 2057, "차단한 유저가 없습니다"),



    //recipe response
    RECIPE_NOT_FOUND(HttpStatus.OK, 2100, "조회된 목록이 없습니다"),

    // market response

    WATCHED_NOT_FOUND(HttpStatus.OK, 2150, "조회했던 아이템이 없습니다."),



    // error Codes

    //FORBIDDEN
    JWT_FORBIDDEN(HttpStatus.OK, 4000, "이미 로그아웃 된 토큰입니다."),
    //FORBIDDEN
    FORBIDDEN(HttpStatus.OK, 4001, "접근 권한이 없습니다."),
    //BAD_REQUEST
    BAD_REQUEST(HttpStatus.OK,4002 ,"잘못된 요청 입니다."),
    //UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.OK, 4003, "UnAuthorized"),
    //UNAUTHORIZED
    JWT_BAD_REQUEST(HttpStatus.OK, 4004,"잘못된 JWT 서명입니다."),
    //UNAUTHORIZED
    JWT_ACCESS_TOKEN_EXPIRED(HttpStatus.OK, 4005,"액세스 토큰이 만료되었습니다."),
    //UNAUTHORIZED
    JWT_REFRESH_TOKEN_EXPIRED(HttpStatus.OK, 4006,"리프레시 토큰이 만료되었습니다. 다시 로그인하시기 바랍니다."),
    //UNAUTHORIZED
    JWT_UNSUPPORTED_TOKEN(HttpStatus.OK, 4007,"지원하지 않는 JWT 토큰입니다."),
    //UNAUTHORIZED
    JWT_TOKEN_NOT_FOUND(HttpStatus.OK, 4008,"유효한 JWT 토큰이 없습니다."),
    //BAD_REQUEST
    FEIGN_CLIENT_ERROR_400(HttpStatus.OK, 4009, "feign에서 400번대 에러가 발생했습니다."),
    //NOT_FOUND
    NOTIFICATION_NOT_FOUND(HttpStatus.OK, 4010, "공지를 찾을 수 없습니다."),


    //BAD_REQUEST
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.OK, 4050,"refresh token이 필요합니다."),
    //BAD_REQUEST
    LOGOUT_FAIL(HttpStatus.OK, 4051, "fcmToken, serialNumber 주세요"),
    //UNAUTHORIZED
    MEMBER_NOT_FOUND(HttpStatus.OK, 4052,"해당 사용자가 존재하지 않습니다"),
    //BAD_REQUEST
    NO_CATEGORY_EXIST(HttpStatus.OK, 4053, "선호하는 음료 카테고리가 잘못 되었습니다."),
    //BAD_REQUEST
    UNDER_PAGE_INDEX_ERROR(HttpStatus.OK, 4054, "페이지 번호는 1 이상이여야 합니다."),
    //BAD_REQUEST
    OVER_PAGE_INDEX_ERROR(HttpStatus.OK, 4055, "페이지 번호가 페이징 범위를 초과했습니다."),
    //BAD_REQUEST
    PHONE_AUTH_NOT_FOUND(HttpStatus.OK, 4056, "인증 번호 요청이 필요합니다."),
    //BAD_REQUEST
    PHONE_AUTH_ERROR(HttpStatus.OK, 4057, "잘못된 인증 번호 입니다."),
    //BAD_REQUEST
    PHONE_AUTH_TIMEOUT(HttpStatus.OK, 4058, "인증 시간이 초과되었습니다."),

    //FORBIDDEN
    TEMP_MEMBER_FORBIDDEN(HttpStatus.OK, 4059, "해당 기능은 로그인을 해야 합니다."),
    //BAD_REQUEST
    NO_REPORT_EXIST(HttpStatus.OK, 4060, "해당 id를 가진 신고 목록이 없습니다. 잘못 보내줬어요"),

    //BAD_REQUEST
    DEREGISTER_FAIL(HttpStatus.OK, 4061, "탈퇴할 수 없는 유저입니다. 탈퇴 불가 사유가 존재합니다."),

    ALREADY_BLOCKED_MEMBER(HttpStatus.OK, 4062, "이미 차단된 사용자입니다."),
    BLOCK_SELF(HttpStatus.OK, 4063, "자신을 차단할 수 없습니다."),

    // BAD_REQUEST
    TARGET_MEMBER_NOT_FOUND(HttpStatus.OK, 4064,"대상 사용자가 없습니다.."),

    //FORBIDDEN
    SELF_FOLLOW_FORBIDDEN(HttpStatus.OK, 4065, "스스로 팔로우는 안됩니다."),

    BLOCKED_MEMBER(HttpStatus.OK, 4066, "내가 차단한 사용자입니다."),


    // market error

    //recipe error

    //BAD_REQUEST
    NULL_RECIPE_ERROR(HttpStatus.OK, 4100, "레시피 작성시 누락된 내용이 있습니다."),
    //BAD_REQUEST
    NO_RECIPE_EXIST(HttpStatus.OK, 4101, "해당 레시피가 존재하지 않습니다."),
    //BAD_REQUEST
    BLOCKED_USER_RECIPE(HttpStatus.OK, 4102, "차단한 사용자의 레시피입니다."),
    //BAD_REQUEST
    WRITTEN_BY_TYPE_ERROR(HttpStatus.OK, 4103, "레시피 작성자 타입이 잘못되었습니다. all, influencer, common중 하나로 보내주세요."),
    //BAD_REQUEST
    ORDER_BY_TYPE_ERROR(HttpStatus.OK, 4104, "조회 방식 타입이 잘못되었습니다. likes, views, latest중 하나로 보내주세요."),
    //BAD_REQUEST
    NO_RECIPE_CATEGORY_EXIST(HttpStatus.OK, 4105, "해당 id를 가진 레시피 카테고리가 없습니다. 잘못 보내줬어요"),
    //BAD_REQUEST
    NOT_RECIPE_OWNER(HttpStatus.OK, 4106, "본인이 작성한 레시피가 아닙니다. 변경할 수 없습니다"),
    //BAD_REQUEST
    NO_COMMENT_EXIST(HttpStatus.OK, 4107, "해당 댓글이 존재하지 않습니다."),
    //BAD_REQUEST
    NOT_COMMENT_OWNER(HttpStatus.OK, 4108, "본인이 작성한 댓글이 아닙니다. 변경할 수 없습니다"),
    //BAD_REQUEST
    RECIPE_OWNER(HttpStatus.OK, 4109, "본인의 레시피입니다. 좋아요/스크랩/신고/차단할 수 없습니다"),
    //BAD_REQUEST
    COMMENT_OWNER(HttpStatus.OK, 4110, "본인의 댓글입니다. 좋아요/스크랩/신고/차단할 수 없습니다"),
    //BAD_REQUEST
    NO_TEMP_RECIPE_EXIST(HttpStatus.OK, 4111, "해당 임시저장 Id가 존재하지 않습니다."),


    //INTERNAL_SERVER_ERROR
    INTERNAL_ERROR(HttpStatus.OK, 5000, "Internal server Error"),
    //INTERNAL_SERVER_ERROR
    FEIGN_CLIENT_ERROR_500(HttpStatus.OK, 5001, "Inter server Error in feign client");



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

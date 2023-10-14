package zipdabang.server.apiPayload.code;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum CommonStatus implements BaseCode {
    OK(HttpStatus.OK,2000, "Ok"),

    OAUTH_LOGIN(HttpStatus.OK,2050, "로그인 입니다."),
    OAUTH_JOIN(HttpStatus.OK,2051,"회원가입 입니다."),
    NICKNAME_EXIST(HttpStatus.OK,2052, "닉네임이 이미 존재합니다."),
    NICKNAME_OK(HttpStatus.OK,2053, "사용 가능한 닉네임 입니다."),
    PHONE_NUMBER_EXIST(HttpStatus.OK, 2054, "이미 인증된 전화번호입니다."),
    AUTO_LOGIN_MAIN(HttpStatus.OK, 2055, "홈 화면으로 이동하세요"),
    AUTO_LOGIN_NOT_MAIN(HttpStatus.OK, 2056, "로그인 화면으로 이동하세요"),
    BLOCKED_MEMBER_NOT_FOUND(HttpStatus.OK, 2057, "차단한 유저가 없습니다"),
    NICKNAME_MEMBER_NOT_EXIST(HttpStatus.OK, 2058, "해당 키워드를 포함한 닉네임을 가진 유저가 없습니다."),


    // recipe response

    RECIPE_NOT_FOUND(HttpStatus.OK, 2100, "조회된 레시피 목록이 없습니다"),
    COMMENT_NOT_FOUND(HttpStatus.OK, 2101, "조회된 댓글 목록이 없습니다"),
    TEMP_RECIPE_NOT_FOUND(HttpStatus.OK, 2102, "조회된 임시저장 레시피 목록이 없습니다"),

    // market response

    WATCHED_NOT_FOUND(HttpStatus.OK, 2150, "조회했던 아이템이 없습니다."),



    // error Codes

    JWT_FORBIDDEN(HttpStatus.FORBIDDEN, 4000, "이미 로그아웃 된 토큰입니다."),
    //FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, 4001, "접근 권한이 없습니다."),
    //BAD_REQUEST
    BAD_REQUEST(HttpStatus.BAD_REQUEST,4002 ,"잘못된 요청 입니다."),
    //UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4003, "UnAuthorized"),
    //UNAUTHORIZED
    JWT_BAD_REQUEST(HttpStatus.UNAUTHORIZED, 4004,"잘못된 JWT 서명입니다."),
    //UNAUTHORIZED
    JWT_ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 4005,"액세스 토큰이 만료되었습니다."),
    //UNAUTHORIZED
    JWT_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 4006,"리프레시 토큰이 만료되었습니다. 다시 로그인하시기 바랍니다."),
    //UNAUTHORIZED
    JWT_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, 4007,"지원하지 않는 JWT 토큰입니다."),
    //UNAUTHORIZED
    JWT_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 4008,"유효한 JWT 토큰이 없습니다."),
    //BAD_REQUEST
    FEIGN_CLIENT_ERROR_400(HttpStatus.BAD_REQUEST, 4009, "feign에서 400번대 에러가 발생했습니다."),
    //NOT_FOUND
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, 4010, "공지를 찾을 수 없습니다."),


    // member error

    //BAD_REQUEST
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, 4050,"refresh token이 필요합니다."),
    //BAD_REQUEST
    LOGOUT_FAIL(HttpStatus.BAD_REQUEST, 4051, "fcmToken, serialNumber 주세요"),
    //UNAUTHORIZED
    MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, 4052,"해당 사용자가 존재하지 않습니다"),
    //BAD_REQUEST
    NO_CATEGORY_EXIST(HttpStatus.BAD_REQUEST, 4053, "선호하는 음료 카테고리가 잘못 되었습니다."),
    //BAD_REQUEST
    UNDER_PAGE_INDEX_ERROR(HttpStatus.BAD_REQUEST, 4054, "페이지 번호는 1 이상이여야 합니다."),
    //BAD_REQUEST
    OVER_PAGE_INDEX_ERROR(HttpStatus.BAD_REQUEST, 4055, "페이지 번호가 페이징 범위를 초과했습니다."),
    //BAD_REQUEST
    PHONE_AUTH_NOT_FOUND(HttpStatus.BAD_REQUEST, 4056, "인증 번호 요청이 필요합니다."),
    //BAD_REQUEST
    PHONE_AUTH_ERROR(HttpStatus.BAD_REQUEST, 4057, "잘못된 인증 번호 입니다."),
    //BAD_REQUEST
    PHONE_AUTH_TIMEOUT(HttpStatus.BAD_REQUEST, 4058, "인증 시간이 초과되었습니다."),

    //FORBIDDEN
    TEMP_MEMBER_FORBIDDEN(HttpStatus.FORBIDDEN, 4059, "해당 기능은 로그인을 해야 합니다."),
    //BAD_REQUEST
    NO_REPORT_EXIST(HttpStatus.BAD_REQUEST, 4060, "해당 id를 가진 신고 목록이 없습니다. 잘못 보내줬어요"),

    //BAD_REQUEST
    DEREGISTER_FAIL(HttpStatus.BAD_REQUEST, 4061, "탈퇴할 수 없는 유저입니다. 탈퇴 불가 사유가 존재합니다."),

    ALREADY_BLOCKED_MEMBER(HttpStatus.BAD_REQUEST, 4062, "이미 차단된 사용자입니다."),
    BLOCK_SELF(HttpStatus.BAD_REQUEST, 4063, "자신을 차단할 수 없습니다."),

    // BAD_REQUEST
    TARGET_MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, 4064,"대상 사용자가 없습니다.."),

    //FORBIDDEN
    SELF_FOLLOW_FORBIDDEN(HttpStatus.FORBIDDEN, 4065, "스스로 팔로우는 안됩니다."),

    BLOCKED_MEMBER(HttpStatus.BAD_GATEWAY, 4066, "내가 차단한 사용자입니다."),

    INQUERY_NOT_FOUND(HttpStatus.NOT_FOUND, 4067, "문의가 없습니다."),

    NOT_MY_INQUERY(HttpStatus.BAD_REQUEST, 4068, "로그인 한 사용자의 문의가 아닙니다."),


    // recipe error

    //BAD_REQUEST
    NULL_RECIPE_ERROR(HttpStatus.BAD_REQUEST, 4100, "레시피 작성시 누락된 내용이 있습니다."),
    //BAD_REQUEST
    NO_RECIPE_EXIST(HttpStatus.BAD_REQUEST, 4101, "해당 레시피가 존재하지 않습니다."),
    //BAD_REQUEST
    BLOCKED_USER_RECIPE(HttpStatus.BAD_REQUEST, 4102, "차단한 사용자의 레시피입니다."),
    //BAD_REQUEST
    WRITTEN_BY_TYPE_ERROR(HttpStatus.BAD_REQUEST, 4103, "레시피 작성자 타입이 잘못되었습니다. official, barista, common중 하나로 보내주세요."),
    //BAD_REQUEST
    ORDER_BY_TYPE_ERROR(HttpStatus.BAD_REQUEST, 4104, "조회 방식 타입이 잘못되었습니다. likes, follow, latest중 하나로 보내주세요."),
    //BAD_REQUEST
    NO_RECIPE_CATEGORY_EXIST(HttpStatus.BAD_REQUEST, 4105, "해당 id를 가진 레시피 카테고리가 없습니다. 잘못 보내줬어요"),
    //BAD_REQUEST
    NOT_RECIPE_OWNER(HttpStatus.BAD_REQUEST, 4106, "본인이 작성한 레시피가 아닙니다. 변경할 수 없습니다"),
    //BAD_REQUEST
    NO_COMMENT_EXIST(HttpStatus.BAD_REQUEST, 4107, "해당 댓글이 존재하지 않습니다."),
    //BAD_REQUEST
    NOT_COMMENT_OWNER(HttpStatus.BAD_REQUEST, 4108, "본인이 작성한 댓글이 아닙니다. 변경할 수 없습니다"),
    //BAD_REQUEST
    RECIPE_OWNER(HttpStatus.BAD_REQUEST, 4109, "본인의 레시피입니다. 좋아요/스크랩/신고/차단할 수 없습니다"),
    //BAD_REQUEST
    COMMENT_OWNER(HttpStatus.BAD_REQUEST, 4110, "본인의 댓글입니다. 좋아요/스크랩/신고/차단할 수 없습니다"),
    //BAD_REQUEST
    NO_TEMP_RECIPE_EXIST(HttpStatus.BAD_REQUEST, 4111, "해당 임시저장 Id가 존재하지 않습니다."),
    NOT_MATCH_RECIPE(HttpStatus.BAD_REQUEST, 4112, "해당 댓글은 넘겨준 레시피 Id에 존재하지 않습니다. 레시피 Id를 올바르게 보내주세요"),


    // market error


    //INTERNAL_SERVER_ERROR
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "Internal server Error"),
    //INTERNAL_SERVER_ERROR
    FEIGN_CLIENT_ERROR_500(HttpStatus.INTERNAL_SERVER_ERROR, 5001, "Inter server Error in feign client");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public Reason getReason() {
        return Reason.builder()
                .message(message)
                .code(code)
                .isSuccess((code >= 2000 && code < 3000) ? true : false)
                .build();
    }

    @Override
    public Reason getReasonHttpStatus() {
        return Reason.builder()
                .message(message)
                .code(code)
                .isSuccess((code >= 2000 && code < 3000) ? true : false)
                .httpStatus(httpStatus)
                .build()
                ;
    }

}

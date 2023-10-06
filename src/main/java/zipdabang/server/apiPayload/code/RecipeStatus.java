package zipdabang.server.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RecipeStatus implements BaseCode{

    RECIPE_NOT_FOUND(HttpStatus.OK, 2100, "조회된 목록이 없습니다"),

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
    NO_TEMP_RECIPE_EXIST(HttpStatus.OK, 4111, "해당 임시저장 Id가 존재하지 않습니다.");

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

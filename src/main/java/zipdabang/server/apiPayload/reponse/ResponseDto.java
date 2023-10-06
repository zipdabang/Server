package zipdabang.server.apiPayload.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import zipdabang.server.apiPayload.code.BaseCode;
import zipdabang.server.apiPayload.code.CommonStatus;


@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
@Schema(description = "기본 응답")
public class ResponseDto<T> {

    @Schema(description = "성공 유무", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    @Schema(description = "응답 코드", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000")
    private final Integer code;
    @Schema(description = "응답 메시지", requiredMode = Schema.RequiredMode.REQUIRED, example = "요청에 성공하였습니다.")
    private final String message;
    @Schema(description = "응답 결과", requiredMode = Schema.RequiredMode.REQUIRED, example = "응답 결과")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;


    // 성공한 경우 응답 생성

    public static <T> ResponseDto<T> of(T result){
        return new ResponseDto<>(true, 2000 , CommonStatus.OK.getMessage(), result);
    }

    public static <T> ResponseDto<T> of(BaseCode code,T result){
        return new ResponseDto<>(true, 2000 , code.getReasonHttpStatus().getMessage(), result);
    }

    // 실패한 경우 응답 생성
    public static <T> ResponseDto<T> onFailure(Integer code, String message, T data){
        return new ResponseDto<>(true, code, message, data);
    }
}

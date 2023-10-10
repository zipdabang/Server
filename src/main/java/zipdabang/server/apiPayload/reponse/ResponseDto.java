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

    @JsonProperty("isSuccess")
    private final Boolean isSuccess;
    private final Integer code;
    private final String message;
    private T result;


    // 성공한 경우 응답 생성

    public static <T> ResponseDto<T> of(T result){
        return new ResponseDto<>(true, 2000 , CommonStatus.OK.getMessage(), result);
    }

    public static <T> ResponseDto<T> of(BaseCode code,T result){
        ResponseDto<T> tResponseDto = new ResponseDto<>(true, code.getReasonHttpStatus().getCode(), code.getReasonHttpStatus().getMessage(), result);
        return tResponseDto;
    }

    // 실패한 경우 응답 생성
    public static <T> ResponseDto<T> onFailure(Integer code, String message, T data){
        return new ResponseDto<>(true, code, message, data);
    }
}

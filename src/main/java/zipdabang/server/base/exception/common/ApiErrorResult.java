package zipdabang.server.base.exception.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiErrorResult {

    private Boolean isSuccess;
    private Integer code;
    private String message;
    private Object result;

    @Override
    public String toString(){
        try{
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }
}

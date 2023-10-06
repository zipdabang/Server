package zipdabang.server.apiPayload.exception.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import zipdabang.server.apiPayload.code.BaseCode;
import zipdabang.server.apiPayload.code.Reason;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private BaseCode code;

    public Reason getErrorReason() {
        return this.code.getReason();
    }

    public Reason getErrorReasonHttpStatus(){
        return this.code.getReasonHttpStatus();
    }
}

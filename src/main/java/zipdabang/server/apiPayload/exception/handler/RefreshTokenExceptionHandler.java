package zipdabang.server.apiPayload.exception.handler;


import zipdabang.server.apiPayload.code.BaseCode;
import zipdabang.server.apiPayload.exception.base.GeneralException;

public class RefreshTokenExceptionHandler extends GeneralException {
    public RefreshTokenExceptionHandler(BaseCode errorCommonStatus) {
        super(errorCommonStatus);
    }
}

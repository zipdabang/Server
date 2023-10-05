package zipdabang.server.apiPayload.exception.handler;


import zipdabang.server.apiPayload.code.BaseCode;
import zipdabang.server.apiPayload.exception.base.GeneralException;

public class RootException extends GeneralException {
    public RootException(BaseCode errorCommonStatus) {
        super(errorCommonStatus);
    }
}

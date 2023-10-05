package zipdabang.server.apiPayload.exception.handler;

import zipdabang.server.apiPayload.code.BaseCode;
import zipdabang.server.apiPayload.exception.base.GeneralException;

public class AuthNumberException extends GeneralException {
    public AuthNumberException(BaseCode errorCode) {
        super(errorCode);
    }
}

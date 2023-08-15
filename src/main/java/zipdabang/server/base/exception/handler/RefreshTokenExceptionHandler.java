package zipdabang.server.base.exception.handler;

import zipdabang.server.base.Code;
import zipdabang.server.base.exception.GeneralException;

public class RefreshTokenExceptionHandler extends GeneralException {
    public RefreshTokenExceptionHandler(Code errorCode) {
        super(errorCode);
    }
}

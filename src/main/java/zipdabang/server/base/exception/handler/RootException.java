package zipdabang.server.base.exception.handler;

import zipdabang.server.base.Code;
import zipdabang.server.base.exception.GeneralException;

public class RootException extends GeneralException {
    public RootException(Code errorCode) {
        super(errorCode);
    }
}

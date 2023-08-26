package zipdabang.server.base.exception.handler;

import zipdabang.server.base.Code;
import zipdabang.server.base.exception.GeneralException;

public class AuthNumberException extends GeneralException {
    public AuthNumberException(Code errorCode){
        super(errorCode);
    }
}

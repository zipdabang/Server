package zipdabang.server.base.exception.handler;

import zipdabang.server.base.Code;
import zipdabang.server.base.exception.GeneralException;

public class MemberException extends GeneralException {
    public MemberException(Code errorCode) {
        super(errorCode);
    }
}

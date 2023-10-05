package zipdabang.server.apiPayload.exception.handler;


import zipdabang.server.apiPayload.code.BaseCode;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.apiPayload.exception.base.GeneralException;

public class MemberException extends GeneralException {
    public MemberException(BaseCode errorCommonStatus) {
        super(errorCommonStatus);
    }
}

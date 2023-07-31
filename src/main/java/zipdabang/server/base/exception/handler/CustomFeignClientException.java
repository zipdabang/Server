package zipdabang.server.base.exception.handler;

import zipdabang.server.base.Code;
import zipdabang.server.base.exception.GeneralException;


public class CustomFeignClientException extends GeneralException {

    public CustomFeignClientException(Code errorCode){
        super(errorCode);
    }
}

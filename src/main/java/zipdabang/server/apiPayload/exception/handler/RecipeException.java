package zipdabang.server.apiPayload.exception.handler;

import zipdabang.server.apiPayload.code.BaseCode;
import zipdabang.server.apiPayload.exception.base.GeneralException;


public class RecipeException extends GeneralException {
    public RecipeException(BaseCode errorCommonStatus) {
        super(errorCommonStatus);
    }
}

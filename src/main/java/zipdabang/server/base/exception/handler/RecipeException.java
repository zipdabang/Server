package zipdabang.server.base.exception.handler;

import zipdabang.server.base.Code;
import zipdabang.server.base.exception.GeneralException;

public class RecipeException extends GeneralException {
    public RecipeException(Code errorCode) {
        super(errorCode);
    }
}

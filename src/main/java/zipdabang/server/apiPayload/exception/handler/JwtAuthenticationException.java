package zipdabang.server.apiPayload.exception.handler;

import org.springframework.security.core.AuthenticationException;
import zipdabang.server.apiPayload.code.BaseCode;
import zipdabang.server.apiPayload.code.CommonStatus;

public class JwtAuthenticationException extends AuthenticationException {

    public JwtAuthenticationException(CommonStatus code){
        super(code.name());
    }
}

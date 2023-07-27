package zipdabang.server.base.exception.handler;

import org.springframework.security.core.AuthenticationException;
import zipdabang.server.base.Code;

public class JwtAuthenticationException extends AuthenticationException {

    public JwtAuthenticationException(Code code){
        super(code.name());
    }
}

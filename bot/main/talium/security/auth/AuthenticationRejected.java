package talium.security.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationRejected extends AuthenticationException {
    HttpStatus status;

    public AuthenticationRejected(HttpStatus status, String body) {
        super(body);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}

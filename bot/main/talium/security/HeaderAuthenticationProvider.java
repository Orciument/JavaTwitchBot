package talium.security;

import org.springframework.http.HttpStatus;
import talium.security.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import talium.security.auth.AuthenticationRejected;

@Service
public class HeaderAuthenticationProvider implements AuthenticationProvider {

    AuthService authService;

    @Autowired
    public HeaderAuthenticationProvider(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            throw new AuthenticationRejected(HttpStatus.BAD_REQUEST, "Token header does not exist");
        }
        String userAgent = (String) authentication.getPrincipal();

        String accessToken = (String) authentication.getCredentials();
        boolean authenticated = authService.authenticate(accessToken, userAgent);

        return new UserAuthenticationToken(accessToken, authenticated);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == PreAuthenticatedAuthenticationToken.class;
    }
}

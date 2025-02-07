package talium.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import talium.security.auth.AuthService;
import talium.security.auth.AuthenticationRejected;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.util.matcher.*;

import java.io.IOException;


public class HeaderAuthProcessingFilter extends AbstractAuthenticationProcessingFilter {

    public HeaderAuthProcessingFilter(AuthenticationManager authenticationManager) {
        super(new NegatedRequestMatcher(
                new OrRequestMatcher(
                        new AntPathRequestMatcher("/error"),
                        new AntPathRequestMatcher("/auth/**")
                )
        ), authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        var token = request.getHeader("token");
        var userAgent = request.getHeader("User-Agent");
        var auth = new PreAuthenticatedAuthenticationToken(userAgent, token);
        if (AuthService.byPassAllAuth) {
            return auth;
        }
        try {
            return this.getAuthenticationManager().authenticate(auth);
        } catch (AuthenticationRejected e) {
            response.sendError(e.status().value(), STR."Authentication failed: \{e.getMessage()}");
            return null;
        }
    }

    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

}

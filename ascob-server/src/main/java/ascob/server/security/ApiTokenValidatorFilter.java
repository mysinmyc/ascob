package ascob.server.security;

import ascob.security.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Component
public class ApiTokenValidatorFilter extends GenericFilterBean
{
    ApiTokenValidator apiTokenValidator;

    static String TOKEN_HEADER="X-Api-Token";

    public ApiTokenValidatorFilter(@Autowired  ApiTokenValidator apiTokenValidator) {
        this.apiTokenValidator = apiTokenValidator;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token=((HttpServletRequest)request).getHeader(TOKEN_HEADER);
        if (token!=null && !token.isEmpty()) {
            try {
                ApiTokenIdentity apiTokenIdentity= apiTokenValidator.validateToken(token);
                Authentication authentication =buildAuthentication(apiTokenIdentity);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }catch (InvalidTokenException e) {
                ((HttpServletResponse)response).setStatus(403);
                return;
            }
        }
        chain.doFilter(request,response);
    }

    private Authentication buildAuthentication(ApiTokenIdentity identity) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (identity.getPermissions()!=null && !identity.getPermissions().isEmpty()) {
            identity.getPermissions().forEach(p -> grantedAuthorities.add(new SimpleGrantedAuthority(p.name())));
        }
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                identity,
                null,
                grantedAuthorities
        );
        return result;
    }
}

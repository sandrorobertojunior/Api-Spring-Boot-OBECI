package org.obeci.platform.configs;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class TokenCookieService {

    private final AuthCookieProperties props;

    public TokenCookieService(AuthCookieProperties props) {
        this.props = props;
    }

    public String getCookieName() {
        return props.getName();
    }

    public ResponseCookie createAuthCookie(String token) {
        // Centraliza a criação do cookie do JWT para evitar duplicação e garantir
        // que DEV/PROD usem as flags corretas (secure/sameSite/domain/maxAge).
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(props.getName(), token)
                .httpOnly(props.isHttpOnly())
                .secure(props.isSecure())
                .sameSite(props.getSameSite())
                .path(props.getPath())
                .maxAge(props.getMaxAgeSeconds());

        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            builder.domain(props.getDomain());
        }

        return builder.build();
    }

    public ResponseCookie clearAuthCookie() {
        // Cookie expirado (Max-Age=0) para logout/invalidar sessão do cliente.
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(props.getName(), "")
                .httpOnly(props.isHttpOnly())
                .secure(props.isSecure())
                .sameSite(props.getSameSite())
                .path(props.getPath())
                .maxAge(0);

        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            builder.domain(props.getDomain());
        }

        return builder.build();
    }
}

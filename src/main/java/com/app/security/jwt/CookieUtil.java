package com.app.security.jwt;

import com.app.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE  = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final JwtProperties jwtProperties;

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, ACCESS_TOKEN_COOKIE, token, (int) (jwtProperties.getAccessTokenExpiration() / 1000));
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, REFRESH_TOKEN_COOKIE, token, (int) (jwtProperties.getRefreshTokenExpiration() / 1000));
    }

    public void clearAuthCookies(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE);
        deleteCookie(response, REFRESH_TOKEN_COOKIE);
    }

    public Optional<String> extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    // ─── Internal ───────────────────────────────────────────────────────────

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);   // passer à true en prod (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        // SameSite via header pour compatibilité maximale
        response.addHeader("Set-Cookie",
                buildSetCookieHeader(name, value, maxAge));
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        response.addHeader("Set-Cookie",
                buildSetCookieHeader(name, "", 0));
    }

    private String buildSetCookieHeader(String name, String value, int maxAge) {
        return String.format("%s=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=Strict",
                name, value, maxAge);
    }
}

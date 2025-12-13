package ru.urasha.callmeani.dream_marketplace.web.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.urasha.callmeani.dream_marketplace.config.JwtProperties;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;
import ru.urasha.callmeani.dream_marketplace.service.YandexOAuthService;
import ru.urasha.callmeani.dream_marketplace.security.JwtService;
import ru.urasha.callmeani.dream_marketplace.web.dto.AuthResponse;
import ru.urasha.callmeani.dream_marketplace.web.mapper.UserMapper;

@RestController
@RequestMapping("/oauth/yandex")
public class AuthController {

    private final YandexOAuthService yandexOAuthService;
    private final UserAccountService userAccountService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthController(YandexOAuthService yandexOAuthService,
                          UserAccountService userAccountService,
                          JwtService jwtService,
                          JwtProperties jwtProperties) {
        this.yandexOAuthService = yandexOAuthService;
        this.userAccountService = userAccountService;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam(value = "state", required = false) String state) {
        String url = yandexOAuthService.buildAuthorizationUrl(state);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", url)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<AuthResponse> callback(@RequestParam(name = "code", required = false) String code,
                                                 @RequestParam(name = "error", required = false) String error,
                                                 HttpServletResponse response) {
        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (code == null) {
            return ResponseEntity.badRequest().build();
        }

        var profile = yandexOAuthService.exchangeCode(code);
        var user = userAccountService.findOrCreateFromYandex(profile);
        String token = jwtService.generateToken(user);

        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtProperties.getAccessTokenTtlSeconds());
        response.addCookie(cookie);

        return ResponseEntity.ok(AuthResponse.bearer(token, UserMapper.toDto(user)));
    }
}

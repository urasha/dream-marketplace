package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.config.YandexOAuthProperties;
import ru.urasha.callmeani.dream_marketplace.service.dto.YandexProfile;
import ru.urasha.callmeani.dream_marketplace.service.dto.YandexTokenResponse;
import ru.urasha.callmeani.dream_marketplace.service.dto.YandexUserInfoResponse;

@Service
public class YandexOAuthService {

    private static final String TOKEN_URL = "https://oauth.yandex.ru/token";
    private static final String USERINFO_URL = "https://login.yandex.ru/info?format=json";

    private final RestTemplate restTemplate;
    private final YandexOAuthProperties properties;

    public YandexOAuthService(RestTemplate restTemplate, YandexOAuthProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String buildAuthorizationUrl(String state) {
        return "https://oauth.yandex.ru/authorize?response_type=code" +
                "&client_id=" + properties.getClientId() +
                "&redirect_uri=" + properties.getRedirectUri() +
            "&force_confirm=1" +
            (state != null ? "&state=" + state : "");
    }

    public YandexProfile exchangeCode(String code) {
        YandexTokenResponse token = requestToken(code);
        YandexUserInfoResponse info = requestUserInfo(token.getAccessToken());
        return new YandexProfile(
                info.getId(),
                info.getDefaultEmail(),
                info.getDisplayName()
        );
    }

    private YandexTokenResponse requestToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecret());
        body.add("redirect_uri", properties.getRedirectUri());

        try {
            YandexTokenResponse token = restTemplate.postForObject(TOKEN_URL, new HttpEntity<>(body, headers), YandexTokenResponse.class);
            if (token == null || token.getAccessToken() == null || token.getAccessToken().isBlank()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Yandex ID недоступен: не удалось получить токен");
            }
            return token;
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Yandex ID недоступен, попробуйте позже", e);
        }
    }

    private YandexUserInfoResponse requestUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        try {
            var response = restTemplate.exchange(
                    USERINFO_URL,
                    org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(headers),
                    YandexUserInfoResponse.class
            );
            YandexUserInfoResponse body = response.getBody();
            if (body == null || body.getId() == null || body.getDefaultEmail() == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Yandex ID недоступен: пустой ответ профиля");
            }
            return body;
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Yandex ID недоступен, попробуйте позже", e);
        }
    }
}

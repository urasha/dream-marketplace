package ru.urasha.callmeani.dream_marketplace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.config.YooKassaProperties;
import ru.urasha.callmeani.dream_marketplace.service.dto.YooKassaPaymentResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class YooKassaClient {

    private static final Logger log = LoggerFactory.getLogger(YooKassaClient.class);

    private static final String BASE_URL = "https://api.yookassa.ru/v3";

    private final RestTemplate restTemplate;
    private final YooKassaProperties props;
    private final ObjectMapper objectMapper;

    public YooKassaClient(RestTemplate restTemplate, YooKassaProperties props, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    public YooKassaPaymentResponse createPayment(BigDecimal amountRub, String description) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Idempotence-Key", UUID.randomUUID().toString());

        Map<String, Object> body = Map.of(
            "amount", Map.of(
                "value", amountRub.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "currency", "RUB"
            ),
            "capture", true,
            "description", description,
            "payment_method_data", Map.of(
                "type", "bank_card"
            ),
            "confirmation", Map.of(
                "type", "redirect",
                "return_url", props.getSuccessUrl()
            )
        );

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/payments", new HttpEntity<>(body, headers), String.class);
            return parseResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            String responseBody = safeBody(e.getResponseBodyAsString());
            log.error("YooKassa createPayment 4xx: status={}, body={}", e.getStatusCode(), responseBody);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YooKassa createPayment failed: " + responseBody, e);
        }
    }

    public YooKassaPaymentResponse getPayment(String paymentId) {
        HttpHeaders headers = authHeaders();
        try {
            ResponseEntity<String> response = restTemplate.exchange(BASE_URL + "/payments/" + paymentId, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return parseResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            String responseBody = safeBody(e.getResponseBodyAsString());
            log.error("YooKassa getPayment 4xx: status={}, body={}", e.getStatusCode(), responseBody);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "YooKassa getPayment failed: " + responseBody, e);
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String basic = props.getShopId() + ":" + props.getSecretKey();
        String encoded = java.util.Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encoded);
        headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
        return headers;
    }

    private YooKassaPaymentResponse parseResponse(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            return new YooKassaPaymentResponse(
                    node.path("id").asText(null),
                    node.path("status").asText(null),
                    node.path("paid").asBoolean(false),
                    node.path("confirmation").path("confirmation_url").asText(null)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse YooKassa response", e);
        }
    }

    private String safeBody(String body) {
        if (body == null) {
            return "<empty>";
        }
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }
}

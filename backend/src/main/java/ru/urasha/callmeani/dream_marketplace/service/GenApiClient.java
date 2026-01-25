package ru.urasha.callmeani.dream_marketplace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.urasha.callmeani.dream_marketplace.config.GenApiProperties;
import ru.urasha.callmeani.dream_marketplace.service.dto.GenApiCreateResponse;
import ru.urasha.callmeani.dream_marketplace.service.dto.GenApiMidjourneyRequest;
import ru.urasha.callmeani.dream_marketplace.service.dto.GenApiStatusResponse;

import java.net.URI;

@Service
public class GenApiClient {

    private final RestTemplate restTemplate;
    private final GenApiProperties props;
    private final ObjectMapper objectMapper;

    public GenApiClient(RestTemplate restTemplate, GenApiProperties props, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.props = props;
        this.objectMapper = objectMapper;
    }

    public GenApiCreateResponse createMidjourneyTask(GenApiMidjourneyRequest request) {
        HttpHeaders headers = defaultHeaders();
        RequestEntity<GenApiMidjourneyRequest> entity = RequestEntity
                .post(URI.create(props.getBaseUrl() + "/networks/midjourney"))
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request);

        ResponseEntity<String> response = restTemplate.exchange(entity, String.class);
        JsonNode body = parseBody(response.getBody());
        return objectMapper.convertValue(body, GenApiCreateResponse.class);
    }

    public GenApiStatusResponse getStatus(String requestId) {
        HttpHeaders headers = defaultHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = props.getBaseUrl() + "/request/get/" + requestId;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        JsonNode body = parseBody(response.getBody());
        return objectMapper.convertValue(body, GenApiStatusResponse.class);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(props.getApiKey());
        headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
        return headers;
    }

    private JsonNode parseBody(String raw) {
        try {
            return objectMapper.readTree(raw != null ? raw : "{}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse GenAPI response", e);
        }
    }
}

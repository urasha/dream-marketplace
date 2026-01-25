package ru.urasha.callmeani.dream_marketplace.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenApiStatusResponse {
    @JsonProperty("request_id")
    private String requestId;
    private String status;
    @JsonProperty("response_type")
    private String responseType;
    private JsonNode output;
    @JsonProperty("result")
    private JsonNode result;
}

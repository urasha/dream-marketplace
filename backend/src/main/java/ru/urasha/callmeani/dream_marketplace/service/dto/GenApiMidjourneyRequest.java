package ru.urasha.callmeani.dream_marketplace.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenApiMidjourneyRequest {
    @JsonProperty("modelstring")
    String modelString;
    String prompt;
    @JsonProperty("aspectRatio")
    String aspectRatio;
    Integer chaos;
    String quality;
    Integer stop;
    String style;
    Integer stylize;
    Boolean tile;
    Integer weird;
    @JsonProperty("callback_url")
    String callbackUrl;
    @JsonProperty("translate_input")
    Boolean translateInput;
    @JsonProperty("upgrade_prompt")
    Boolean upgradePrompt;
    @JsonProperty("styleReference")
    String styleReference;
    @JsonProperty("characterReference")
    String characterReference;
}

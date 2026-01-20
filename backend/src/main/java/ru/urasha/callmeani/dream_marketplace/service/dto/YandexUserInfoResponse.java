package ru.urasha.callmeani.dream_marketplace.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YandexUserInfoResponse {

    private String id;
    private String login;

    @JsonProperty("default_email")
    private String defaultEmail;

    @JsonProperty("real_name")
    private String realName;

    @JsonProperty("default_avatar_id")
    private String defaultAvatarId;

    @JsonProperty("is_avatar_empty")
    private Boolean isAvatarEmpty;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getDefaultEmail() {
        return defaultEmail;
    }

    public void setDefaultEmail(String defaultEmail) {
        this.defaultEmail = defaultEmail;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getDefaultAvatarId() {
        return defaultAvatarId;
    }

    public void setDefaultAvatarId(String defaultAvatarId) {
        this.defaultAvatarId = defaultAvatarId;
    }

    public Boolean getIsAvatarEmpty() {
        return isAvatarEmpty;
    }

    public void setIsAvatarEmpty(Boolean isAvatarEmpty) {
        this.isAvatarEmpty = isAvatarEmpty;
    }

    public String getDisplayName() {
        if (realName != null && !realName.isBlank()) {
            return realName;
        }
        if (login != null && !login.isBlank()) {
            return login;
        }
        return defaultEmail;
    }

    public String getAvatarUrl() {
        if (Boolean.TRUE.equals(isAvatarEmpty)) {
            return null;
        }
        if (defaultAvatarId == null || defaultAvatarId.isBlank()) {
            return null;
        }
        return "https://avatars.yandex.net/get-yapic/" + defaultAvatarId + "/islands-200";
    }
}

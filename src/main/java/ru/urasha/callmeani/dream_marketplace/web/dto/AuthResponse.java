package ru.urasha.callmeani.dream_marketplace.web.dto;

public record AuthResponse(String token, String tokenType, UserDto user) {
    public static AuthResponse bearer(String token, UserDto user) {
        return new AuthResponse(token, "Bearer", user);
    }
}

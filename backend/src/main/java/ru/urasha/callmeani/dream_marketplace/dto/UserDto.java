package ru.urasha.callmeani.dream_marketplace.dto;

import ru.urasha.callmeani.dream_marketplace.models.enums.UserRole;

public record UserDto(Long id, String username, String email, String yandexId, String avatarUrl, UserRole role) {
}

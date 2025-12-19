package ru.urasha.callmeani.dream_marketplace.web.dto;

import ru.urasha.callmeani.dream_marketplace.models.enums.UserRole;

public record UserDto(Long id, String username, String email, String yandexId, UserRole role) {
}

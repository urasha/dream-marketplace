package ru.urasha.callmeani.dream_marketplace.security;

import ru.urasha.callmeani.dream_marketplace.models.enums.UserRole;

public record JwtUserDetails(Long userId, String email, String yandexId, UserRole role) {
}

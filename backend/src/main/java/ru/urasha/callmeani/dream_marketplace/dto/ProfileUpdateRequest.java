package ru.urasha.callmeani.dream_marketplace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(min = 1, max = 255) String username,
        @Email String email
) {
}

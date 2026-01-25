package ru.urasha.callmeani.dream_marketplace.mappers;

import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.dto.UserDto;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(UserAccount user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getYandexId(),
                user.getAvatarUrl(),
                user.getRole()
        );
    }
}

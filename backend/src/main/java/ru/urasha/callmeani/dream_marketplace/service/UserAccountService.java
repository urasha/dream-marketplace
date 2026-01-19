package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.UserRole;
import ru.urasha.callmeani.dream_marketplace.repositories.UserAccountRepository;
import ru.urasha.callmeani.dream_marketplace.service.dto.YandexProfile;

import java.util.Optional;

@Service
public class UserAccountService {

    private final UserAccountRepository userRepository;

    public UserAccountService(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserAccount findOrCreateFromYandex(YandexProfile profile) {
        Optional<UserAccount> existingByYandex = userRepository.findByYandexId(profile.id());
        if (existingByYandex.isPresent()) {
            return ensureAdminBootstrap(existingByYandex.get());
        }

        Optional<UserAccount> existingByEmail = userRepository.findByEmail(profile.email());
        if (existingByEmail.isPresent()) {
            UserAccount user = existingByEmail.get();
            user.setYandexId(profile.id());
            return ensureAdminBootstrap(userRepository.save(user));
        }

        UserAccount user = new UserAccount();
        user.setYandexId(profile.id());
        user.setUsername(profile.displayName());
        user.setEmail(profile.email());
        user.setRole(UserRole.USER);
        return ensureAdminBootstrap(userRepository.save(user));
    }

    private UserAccount ensureAdminBootstrap(UserAccount user) {
        if (!userRepository.existsByRole(UserRole.ADMIN)) {
            user.setRole(UserRole.ADMIN);
            return userRepository.save(user);
        }
        return user;
    }

    public Optional<UserAccount> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public UserAccount updateProfile(Long userId, String username, String email) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String trimmedEmail = email != null ? email.trim() : null;
        if (trimmedEmail != null && !trimmedEmail.isBlank()) {
            userRepository.findByEmail(trimmedEmail)
                    .filter(other -> !other.getId().equals(userId))
                    .ifPresent(other -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
                    });
            user.setEmail(trimmedEmail);
        }

        String trimmedUsername = username != null ? username.trim() : null;
        if (trimmedUsername != null && !trimmedUsername.isBlank()) {
            user.setUsername(trimmedUsername);
        }

        return userRepository.save(user);
    }
}

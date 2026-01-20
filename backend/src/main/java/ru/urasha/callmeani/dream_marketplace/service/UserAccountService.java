package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.UserRole;
import ru.urasha.callmeani.dream_marketplace.repositories.UserAccountRepository;
import ru.urasha.callmeani.dream_marketplace.service.dto.YandexProfile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAccountService {

    private final UserAccountRepository userRepository;
    private final S3StorageService storageService;

    public UserAccountService(UserAccountRepository userRepository, S3StorageService storageService) {
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Transactional
    public UserAccount findOrCreateFromYandex(YandexProfile profile) {
        Optional<UserAccount> existingByYandex = userRepository.findByYandexId(profile.id());
        if (existingByYandex.isPresent()) {
            UserAccount user = existingByYandex.get();
            applyAvatarIfMissing(user, profile.avatarUrl());
            return ensureAdminBootstrap(user);
        }

        Optional<UserAccount> existingByEmail = userRepository.findByEmail(profile.email());
        if (existingByEmail.isPresent()) {
            UserAccount user = existingByEmail.get();
            user.setYandexId(profile.id());
            applyAvatarIfMissing(user, profile.avatarUrl());
            return ensureAdminBootstrap(userRepository.save(user));
        }

        UserAccount user = new UserAccount();
        user.setYandexId(profile.id());
        user.setUsername(profile.displayName());
        user.setEmail(profile.email());
        user.setAvatarUrl(profile.avatarUrl());
        user.setRole(UserRole.USER);
        return ensureAdminBootstrap(userRepository.save(user));
    }

    private void applyAvatarIfMissing(UserAccount user, String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }
        if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
        }
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

    @Transactional
    public UserAccount updateAvatar(Long userId, MultipartFile file) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не выбран");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поддерживаются только PNG, JPG или WEBP");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Файл больше 5 МБ");
        }
        String ext = contentType.equals("image/png") ? "png" : contentType.equals("image/webp") ? "webp" : "jpg";
        String key = "avatars/" + userId + "/" + UUID.randomUUID() + "." + ext;
        try {
            String url = storageService.uploadBytes(key, file.getBytes(), contentType);
            user.setAvatarUrl(url);
            return userRepository.save(user);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось загрузить аватар", e);
        }
    }
}

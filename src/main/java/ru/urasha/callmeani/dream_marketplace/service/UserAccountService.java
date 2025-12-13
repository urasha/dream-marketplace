package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            return existingByYandex.get();
        }

        Optional<UserAccount> existingByEmail = userRepository.findByEmail(profile.email());
        if (existingByEmail.isPresent()) {
            UserAccount user = existingByEmail.get();
            user.setYandexId(profile.id());
            return user;
        }

        UserAccount user = new UserAccount();
        user.setYandexId(profile.id());
        user.setUsername(profile.displayName());
        user.setEmail(profile.email());
        user.setRole(UserRole.USER);
        return userRepository.save(user);
    }

    public Optional<UserAccount> findById(Long id) {
        return userRepository.findById(id);
    }
}

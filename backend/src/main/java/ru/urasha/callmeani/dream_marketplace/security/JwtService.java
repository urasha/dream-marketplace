package ru.urasha.callmeani.dream_marketplace.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;
import ru.urasha.callmeani.dream_marketplace.config.JwtProperties;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.UserRole;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.algorithm = Algorithm.HMAC256(properties.getSecret());
        this.verifier = JWT.require(algorithm)
                .withIssuer(properties.getIssuer())
                .build();
    }

    public String generateToken(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getAccessTokenTtlSeconds());

        return JWT.create()
                .withIssuer(properties.getIssuer())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .withSubject(String.valueOf(user.getId()))
                .withClaim("email", user.getEmail())
                .withClaim("yandexId", user.getYandexId())
                .withClaim("role", user.getRole().name())
                .sign(algorithm);
    }

    public Optional<JwtUserDetails> parse(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            Long userId = Long.parseLong(jwt.getSubject());
            String email = jwt.getClaim("email").asString();
            String yandexId = jwt.getClaim("yandexId").asString();
            String roleValue = jwt.getClaim("role").asString();
            UserRole role = roleValue == null ? UserRole.USER : UserRole.valueOf(roleValue);
            return Optional.of(new JwtUserDetails(userId, email, yandexId, role));
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

package az.company.msauth.service.abstraction;

import io.jsonwebtoken.Claims;

import java.util.function.Function;

public interface JwtService {
    String generateToken(Long userId, String username, String role);

    Claims parseClaims(String token);

    long getRemainingMillis(String token);

    <T> T extract(String token, Function<Claims, T> resolver);

    String extractJti(String token);
}

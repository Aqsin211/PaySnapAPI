package az.company.msauth.service.concrete;

import az.company.msauth.service.abstraction.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtServiceImpl implements JwtService {

    private final Key key;
    private final long tokenValidityMillis;

    public JwtServiceImpl(
            @Value("${jwt.secret-key}") String secret,
            @Value("${jwt.token-validity-ms:3600000}") long tokenValidityMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenValidityMillis = tokenValidityMillis;
    }

    @Override
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + tokenValidityMillis);
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject("PaymentAPI")
                .setId(jti)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public long getRemainingMillis(String token) {
        Date exp = parseClaims(token).getExpiration();
        long rem = exp.getTime() - System.currentTimeMillis();
        return Math.max(rem, 0L);
    }

    @Override
    public <T> T extract(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseClaims(token));
    }

    @Override
    public String extractJti(String token) {
        return extract(token, Claims::getId);
    }

}

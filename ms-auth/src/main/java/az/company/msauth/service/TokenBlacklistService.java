package az.company.msauth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "blacklist:";

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistJti(String jti, long ttlMillis) {
        if (jti == null || ttlMillis <= 0) return;
        String key = PREFIX + jti;
        redisTemplate.opsForValue().set(key, "true", Duration.ofMillis(ttlMillis));
    }

}

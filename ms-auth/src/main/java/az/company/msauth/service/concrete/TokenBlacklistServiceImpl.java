package az.company.msauth.service.concrete;

import az.company.msauth.service.abstraction.TokenBlacklistService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "blacklist:";

    public TokenBlacklistServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistJti(String jti, long ttlMillis) {
        if (jti == null || ttlMillis <= 0) return;
        String key = PREFIX + jti;
        redisTemplate.opsForValue().set(key, "true", Duration.ofMillis(ttlMillis));
    }

}

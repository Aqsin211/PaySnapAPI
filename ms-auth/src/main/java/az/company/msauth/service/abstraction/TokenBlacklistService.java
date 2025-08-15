package az.company.msauth.service.abstraction;

public interface TokenBlacklistService {
    void blacklistJti(String jti, long ttlMillis);
}

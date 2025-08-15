package az.company.msauth.controller;

import az.company.msauth.client.UserClient;
import az.company.msauth.model.request.AuthRequest;
import az.company.msauth.model.response.AuthResponse;
import az.company.msauth.model.response.UserResponse;
import az.company.msauth.service.concrete.JwtServiceImpl;
import az.company.msauth.service.concrete.TokenBlacklistServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtServiceImpl jwtServiceImpl;
    private final UserClient userClient;
    private final TokenBlacklistServiceImpl blacklistService;

    public AuthController(JwtServiceImpl jwtServiceImpl, UserClient userClient, TokenBlacklistServiceImpl blacklistService) {
        this.jwtServiceImpl = jwtServiceImpl;
        this.userClient = userClient;
        this.blacklistService = blacklistService;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> getToken(@RequestBody AuthRequest authRequest) {
        Boolean valid = userClient.userValid("system-id", "USER", authRequest);
        if (Boolean.TRUE.equals(valid)) {
            UserResponse user = userClient.getUserByUsername("system-id", "USER", authRequest.getUsername()).getBody();
            String token = jwtServiceImpl.generateToken(user.getUserId(), user.getUsername(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(token));
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.noContent().build();
        String token = auth.substring(7);
        try {
            String jti = jwtServiceImpl.extractJti(token);
            long ttl = jwtServiceImpl.getRemainingMillis(token);
            if (jti != null && ttl > 0) {
                blacklistService.blacklistJti(jti, ttl);
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.noContent().build();
    }

}

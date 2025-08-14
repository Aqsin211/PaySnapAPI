package az.company.msauth.controller;

import az.company.msauth.client.UserClient;
import az.company.msauth.model.request.AuthRequest;
import az.company.msauth.model.response.AuthResponse;
import az.company.msauth.model.response.UserResponse;
import az.company.msauth.security.JwtService;
import az.company.msauth.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserClient userClient;
    private final TokenBlacklistService blacklistService;

    public AuthController(JwtService jwtService, UserClient userClient, TokenBlacklistService blacklistService) {
        this.jwtService = jwtService;
        this.userClient = userClient;
        this.blacklistService = blacklistService;
    }

    @PostMapping
    public ResponseEntity<AuthResponse> getToken(@RequestBody AuthRequest authRequest) {
        Boolean valid = userClient.userValid("system-id", "USER", authRequest);
        if (Boolean.TRUE.equals(valid)) {
            UserResponse user = userClient.getUserByUsername("system-id", "USER", authRequest.getUsername()).getBody();
            String token = jwtService.generateToken(user.getUserId(), user.getUsername(), user.getRole());
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
            String jti = jwtService.extractJti(token);
            long ttl = jwtService.getRemainingMillis(token);
            if (jti != null && ttl > 0) {
                blacklistService.blacklistJti(jti, ttl);
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.noContent().build();
    }

}

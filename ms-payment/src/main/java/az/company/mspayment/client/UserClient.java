package az.company.mspayment.client;

import az.company.mspayment.client.decoder.CustomErrorDecoder;
import az.company.mspayment.model.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "ms-user",
        url = "http://localhost:8082/user",
        configuration = {CustomErrorDecoder.class}
)
public interface UserClient {
    @GetMapping
    ResponseEntity<UserResponse> getUser(
            @RequestHeader("X-User-ID") Long userId,
            @RequestHeader("X-role") String role
    );
}
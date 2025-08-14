package az.company.mspayment.controller;

import az.company.mspayment.service.WebhookService;
import com.stripe.exception.SignatureVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/stripe/webhook")
public class WebhookController {
    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<Void> handle(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) {
        try {
            webhookService.handle(payload, signature);
            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }



}

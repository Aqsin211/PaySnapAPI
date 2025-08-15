package az.company.mspayment.controller;

import az.company.mspayment.model.request.PaymentRequest;
import az.company.mspayment.model.response.PaymentResponse;
import az.company.mspayment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest paymentRequest,
                                                  @RequestHeader("X-User-ID") Long userId) throws Exception {
        return ResponseEntity.ok(paymentService.createCheckout(userId, paymentRequest));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.get(id));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<PaymentResponse>> history(@RequestHeader("X-User-ID") Long userId) {
        return ResponseEntity.ok(paymentService.history(userId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }

}

package az.company.mspayment.controller;

import az.company.mspayment.exception.NotAuthenticatedException;
import az.company.mspayment.model.request.PaymentRequest;
import az.company.mspayment.model.response.PaymentResponse;
import az.company.mspayment.service.concrete.PaymentServiceImpl;
import az.company.mspayment.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static az.company.mspayment.model.enums.ErrorMessages.NOT_LOGGED;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentServiceImpl paymentServiceImpl;

    public PaymentController(PaymentServiceImpl paymentServiceImpl) {
        this.paymentServiceImpl = paymentServiceImpl;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest paymentRequest) throws Exception {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new NotAuthenticatedException(NOT_LOGGED.getMessage());
        }
        return ResponseEntity.ok(paymentServiceImpl.createCheckout(userId, paymentRequest));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(paymentServiceImpl.get(id));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<PaymentResponse>> history() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new NotAuthenticatedException(NOT_LOGGED.getMessage());
        }
        return ResponseEntity.ok(paymentServiceImpl.history(userId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentServiceImpl.getAll());
    }

}

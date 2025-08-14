package az.company.mspayment.controller;

import az.company.mspayment.dao.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/pay")
public class ShortUrlController {

    private final PaymentRepository paymentRepository;

    public ShortUrlController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/{shortUrl}")
    public void redirectToCheckout(@PathVariable String shortUrl, HttpServletResponse response) throws IOException {
        paymentRepository.findByShortUrl(shortUrl)
                .ifPresentOrElse(payment -> {
                    try {
                        response.sendRedirect(payment.getCheckoutUrl());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, () -> {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                });
    }

}

package az.company.mspayment.controller;

import az.company.mspayment.model.response.PaymentResponse;
import az.company.mspayment.service.PaymentService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/payment/receipt")
public class ReceiptController {

    private final PaymentService paymentService;

    public ReceiptController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public ResponseEntity<FileSystemResource> download(
            @PathVariable Long paymentId) {

        PaymentResponse payment = paymentService.get(paymentId);

        String receiptPath = payment.getReceiptFilePath();
        if (receiptPath == null || receiptPath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(receiptPath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + paymentId + ".pdf")
                .body(resource);
    }
}

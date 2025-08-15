package az.company.mspayment.service.abstraction;

import java.io.File;

public interface NotificationService {
    void sendPaymentStatusEmail(String recipientEmail, String username, String paymentId, String status);

    void sendPaymentReceiptEmail(String recipientEmail, String username, String paymentId, String status, File receiptPdf);
}

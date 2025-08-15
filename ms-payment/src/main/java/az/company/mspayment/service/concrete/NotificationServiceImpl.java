package az.company.mspayment.service.concrete;

import az.company.mspayment.service.abstraction.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String senderEmail;

    public NotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPaymentStatusEmail(String recipientEmail, String username, String paymentId, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setFrom(senderEmail);
            helper.setSubject("Payment Status Update: " + status);

            String text = String.format(
                    "Hello %s,\n\n" +
                            "Your payment with ID %s has been updated.\n" +
                            "Status: %s\n\n" +
                            "If you did not initiate this payment, please contact support immediately.\n\n" +
                            "Thank you,\nPayment Service Team",
                    username,
                    paymentId,
                    status
            );
            helper.setText(text);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    @Override
    public void sendPaymentReceiptEmail(String recipientEmail, String username, String paymentId, String status, File receiptPdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setFrom(senderEmail);
            helper.setSubject("Payment Receipt: " + status);

            String text = String.format(
                    "Hello %s,\n\n" +
                            "Your payment with ID %s was successful.\n" +
                            "Status: %s\n\n" +
                            "Please find your receipt attached.\n\n" +
                            "Thank you,\nPayment Service Team",
                    username,
                    paymentId,
                    status
            );
            helper.setText(text);

            helper.addAttachment("receipt-" + paymentId + ".pdf", new FileSystemResource(receiptPdf));

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send receipt email", e);
        }
    }
}

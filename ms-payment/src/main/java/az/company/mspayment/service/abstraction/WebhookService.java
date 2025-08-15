package az.company.mspayment.service.abstraction;

import az.company.mspayment.dao.entity.PaymentEntity;
import az.company.mspayment.model.enums.PaymentStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;

public interface WebhookService {
    void handle(String payload, String sigHeader) throws SignatureVerificationException;

    void handleSessionEvent(Session session, String type);

    void handlePaymentIntentEvent(PaymentIntent pi, String type);

    void markPaymentSucceedBySession(Session session);

    void markPaymentCompletedByPaymentIntent(PaymentIntent pi);

    void markPaymentFailedByPaymentIntent(PaymentIntent pi);

    void markPaymentFailedBySession(Session session, PaymentStatus status);

    void saveIntentIfMissing(PaymentEntity payment, String stripePaymentIntentId);

    void sendSuccessNotification(PaymentEntity payment);

    void sendFailureNotification(PaymentEntity payment);
}

package az.company.mspayment.service.concrete;

import az.company.mspayment.client.UserClient;
import az.company.mspayment.dao.entity.PaymentEntity;
import az.company.mspayment.dao.repository.PaymentRepository;
import az.company.mspayment.model.enums.PaymentStatus;
import az.company.mspayment.model.response.UserResponse;
import az.company.mspayment.service.abstraction.WebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class WebhookServiceImpl implements WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookServiceImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String endpointSecret;
    private final PaymentRepository paymentRepository;
    private final NotificationServiceImpl notificationServiceImpl;
    private final UserClient userClient;
    private final ReceiptServiceImpl receiptServiceImpl;

    public WebhookServiceImpl(@Value("${stripe.webhook-secret}") String secret,
                              PaymentServiceImpl paymentServiceImpl,
                              PaymentRepository paymentRepository,
                              NotificationServiceImpl notificationServiceImpl,
                              UserClient userClient,
                              ReceiptServiceImpl receiptServiceImpl) {
        this.endpointSecret = secret;
        this.paymentRepository = paymentRepository;
        this.notificationServiceImpl = notificationServiceImpl;
        this.userClient = userClient;
        this.receiptServiceImpl = receiptServiceImpl;
    }

    @Override
    public void handle(String payload, String sigHeader) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        String type = event.getType();
        log.info("[WEBHOOK] Received event: {}", type);

        try {
            Optional<StripeObject> objOpt = event.getDataObjectDeserializer().getObject();

            if (objOpt.isPresent()) {
                Object obj = objOpt.get();
                if (obj instanceof Session session) {
                    handleSessionEvent(session, type);
                    return;
                } else if (obj instanceof PaymentIntent pi) {
                    handlePaymentIntentEvent(pi, type);
                    return;
                }
            }

            JsonNode root = MAPPER.readTree(payload);
            JsonNode dataObj = root.path("data").path("object");
            if (dataObj.isMissingNode()) return;

            String objectType = dataObj.path("object").asText(null);
            String id = dataObj.path("id").asText(null);
            if (objectType == null || id == null) return;

            switch (objectType) {
                case "checkout.session":
                    try {
                        handleSessionEvent(Session.retrieve(id), type);
                    } catch (StripeException se) {
                        log.error(se.getMessage(), se);
                    }
                    break;
                case "payment_intent":
                    try {
                        handlePaymentIntentEvent(PaymentIntent.retrieve(id), type);
                    } catch (StripeException se) {
                        log.error(se.getMessage(), se);
                    }
                    break;
                default:
                    log.info("[WEBHOOK] Unknown object type '{}'", objectType);
            }
        } catch (Exception e) {
            log.error("[WEBHOOK] Unexpected error while handling webhook: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleSessionEvent(Session session, String type) {
        log.info("[WEBHOOK] handleSessionEvent: type={}, sessionId={}, paymentIntent={}", type, session.getId(), session.getPaymentIntent());
        switch (type) {
            case "checkout.session.completed":
            case "checkout.session.async_payment_succeeded":
                markPaymentSucceedBySession(session);
                break;
            case "checkout.session.expired":
                markPaymentFailedBySession(session, PaymentStatus.EXPIRED);
                break;
            case "checkout.session.failed":
            case "checkout.session.async_payment_failed":
                markPaymentFailedBySession(session, PaymentStatus.FAILED);
                break;
            default:
                log.info("[WEBHOOK] Unhandled session event: {}", type);
        }
    }

    @Override
    public void handlePaymentIntentEvent(PaymentIntent pi, String type) {
        log.info("[WEBHOOK] handlePaymentIntentEvent: type={}, piId={}", type, pi.getId());
        switch (type) {
            case "payment_intent.succeeded":
                markPaymentCompletedByPaymentIntent(pi);
                break;
            case "payment_intent.payment_failed":
                markPaymentFailedByPaymentIntent(pi);
                break;
            default:
                log.info("[WEBHOOK] Unhandled payment_intent event: {}", type);
        }
    }

    @Override
    public void markPaymentSucceedBySession(Session session) {
        Optional<PaymentEntity> opt = paymentRepository.findByStripeSessionId(session.getId());
        if (opt.isPresent()) {
            PaymentEntity payment = opt.get();
            saveIntentIfMissing(payment, session.getPaymentIntent());
            if (payment.getStatus() != PaymentStatus.SUCCEED && payment.getStatus() != PaymentStatus.COMPLETED) {
                payment.setStatus(PaymentStatus.SUCCEED);
                payment.setCompletedAt(OffsetDateTime.now());
                paymentRepository.save(payment);
                sendSuccessNotification(payment);
                log.info("[WEBHOOK] Payment {} marked SUCCEED (session)", payment.getId());
            }
            return;
        }

        if (session.getMetadata() != null && session.getMetadata().containsKey("paymentId")) {
            String pid = session.getMetadata().get("paymentId");
            try {
                Long id = Long.parseLong(pid);
                paymentRepository.findById(id).ifPresent(payment -> {
                    saveIntentIfMissing(payment, session.getPaymentIntent());
                    if (payment.getStatus() != PaymentStatus.SUCCEED && payment.getStatus() != PaymentStatus.COMPLETED) {
                        payment.setStatus(PaymentStatus.SUCCEED);
                        payment.setCompletedAt(OffsetDateTime.now());
                        paymentRepository.save(payment);
                        sendSuccessNotification(payment);
                        log.info("[WEBHOOK] Payment {} marked SUCCEED (session.metadata)", payment.getId());
                    }
                });
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public void markPaymentCompletedByPaymentIntent(PaymentIntent pi) {
        Optional<PaymentEntity> opt = paymentRepository.findByStripePaymentIntentId(pi.getId());
        if (opt.isPresent()) {
            PaymentEntity payment = opt.get();
            if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.SUCCEED) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setCompletedAt(OffsetDateTime.now());
                paymentRepository.save(payment);
                sendSuccessNotification(payment);
                log.info("[WEBHOOK] Payment {} marked COMPLETED (payment_intent)", payment.getId());
            }
            return;
        }

        if (pi.getMetadata() != null && pi.getMetadata().containsKey("paymentId")) {
            String pid = pi.getMetadata().get("paymentId");
            try {
                Long id = Long.parseLong(pid);
                paymentRepository.findById(id).ifPresent(payment -> {
                    payment.setStripePaymentIntentId(pi.getId());
                    if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.SUCCEED) {
                        payment.setStatus(PaymentStatus.COMPLETED);
                        payment.setCompletedAt(OffsetDateTime.now());
                        paymentRepository.save(payment);
                        sendSuccessNotification(payment);
                        log.info("[WEBHOOK] Payment {} matched by metadata and marked COMPLETED", payment.getId());
                    }
                });
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public void markPaymentFailedByPaymentIntent(PaymentIntent pi) {
        Optional<PaymentEntity> opt = paymentRepository.findByStripePaymentIntentId(pi.getId());
        if (opt.isPresent()) {
            PaymentEntity payment = opt.get();
            payment.setStatus(PaymentStatus.FAILED);
            payment.setCompletedAt(OffsetDateTime.now());
            paymentRepository.save(payment);
            sendFailureNotification(payment);
            return;
        }

        if (pi.getMetadata() != null && pi.getMetadata().containsKey("paymentId")) {
            String pid = pi.getMetadata().get("paymentId");
            try {
                Long id = Long.parseLong(pid);
                paymentRepository.findById(id).ifPresent(payment -> {
                    payment.setStripePaymentIntentId(pi.getId());
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setCompletedAt(OffsetDateTime.now());
                    paymentRepository.save(payment);
                    sendFailureNotification(payment);
                });
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public void markPaymentFailedBySession(Session session, PaymentStatus status) {
        Optional<PaymentEntity> opt = paymentRepository.findByStripeSessionId(session.getId());
        if (opt.isPresent()) {
            PaymentEntity payment = opt.get();
            payment.setStatus(status);
            payment.setCompletedAt(OffsetDateTime.now());
            paymentRepository.save(payment);
            sendFailureNotification(payment);
            return;
        }

        if (session.getMetadata() != null && session.getMetadata().containsKey("paymentId")) {
            String pid = session.getMetadata().get("paymentId");
            try {
                Long id = Long.parseLong(pid);
                paymentRepository.findById(id).ifPresent(payment -> {
                    payment.setStatus(status);
                    payment.setCompletedAt(OffsetDateTime.now());
                    paymentRepository.save(payment);
                    sendFailureNotification(payment);
                });
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public void saveIntentIfMissing(PaymentEntity payment, String stripePaymentIntentId) {
        if (stripePaymentIntentId != null && (payment.getStripePaymentIntentId() == null || payment.getStripePaymentIntentId().isEmpty())) {
            payment.setStripePaymentIntentId(stripePaymentIntentId);
            paymentRepository.save(payment);
        }
    }

    @Override
    public void sendSuccessNotification(PaymentEntity payment) {
        if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.SUCCEED) return;

        try {
            UserResponse user = userClient.getUser(payment.getUserId(), "USER").getBody();
            if (user != null) {
                Path receipt = receiptServiceImpl.generateReceipt(payment);
                notificationServiceImpl.sendPaymentReceiptEmail(
                        user.getGmail(),
                        user.getUsername(),
                        payment.getId().toString(),
                        payment.getStatus().name(),
                        receipt.toFile()
                );
            }
        } catch (Exception e) {
            log.error("[WEBHOOK] Failed to send success email: {}", e.getMessage(), e);
        }
    }

    @Override
    public void sendFailureNotification(PaymentEntity payment) {
        try {
            UserResponse user = userClient.getUser(payment.getUserId(), "USER").getBody();
            if (user != null) {
                notificationServiceImpl.sendPaymentStatusEmail(
                        user.getGmail(),
                        user.getUsername(),
                        payment.getId().toString(),
                        payment.getStatus().name()
                );
            }
        } catch (Exception e) {
            log.error("[WEBHOOK] Error while sending failure notification: {}", e.getMessage(), e);
        }
    }
}

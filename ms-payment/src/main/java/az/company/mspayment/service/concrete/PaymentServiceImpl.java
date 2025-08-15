package az.company.mspayment.service.concrete;

import az.company.mspayment.dao.entity.PaymentEntity;
import az.company.mspayment.dao.repository.PaymentRepository;
import az.company.mspayment.model.enums.PaymentStatus;
import az.company.mspayment.model.request.PaymentRequest;
import az.company.mspayment.model.response.PaymentResponse;
import az.company.mspayment.service.abstraction.PaymentService;
import az.company.mspayment.util.Base62;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;

import static az.company.mspayment.model.mapper.PaymentMapper.PAYMENT_MAPPER;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final int ttlMinutes;
    private final String successUrl;
    private final String cancelUrl;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              @Value("${stripe.session-ttl-minutes:15}") int ttlMinutes,
                              @Value("${stripe.success-url}") String successUrl,
                              @Value("${stripe.cancel-url}") String cancelUrl) {
        this.paymentRepository = paymentRepository;
        this.ttlMinutes = ttlMinutes;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    @Override
    public PaymentResponse createCheckout(Long userId, PaymentRequest paymentRequest) throws StripeException {
        OffsetDateTime now = OffsetDateTime.now();

        //persisting local payment
        PaymentEntity payment = PaymentEntity.builder()
                .userId(userId)
                .amount(paymentRequest.getAmount())
                .currency(paymentRequest.getCurrency().toUpperCase())
                .description(paymentRequest.getDescription())
                .status(PaymentStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusMinutes(ttlMinutes))
                .build();

        paymentRepository.save(payment);

        log.info("[PAYMENT] Saved initial local payment id={}", payment.getId());

        //Generating Stripe session
        SessionCreateParams.LineItem.PriceData.ProductData product =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(paymentRequest.getDescription())
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(paymentRequest.getCurrency().toLowerCase())
                        .setUnitAmount(paymentRequest.getAmount().movePointRight(2).longValue())
                        .setProductData(product)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(priceData)
                        .build();

        SessionCreateParams.PaymentIntentData paymentIntentData =
                SessionCreateParams.PaymentIntentData.builder()
                        .putMetadata("paymentId", payment.getId().toString())
                        .build();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .addLineItem(lineItem)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .putMetadata("paymentId", payment.getId().toString())
                        .setPaymentIntentData(paymentIntentData)
                        .build();

        Session session = Session.create(params);

        payment.setStripeSessionId(session.getId());

        if (session.getPaymentIntent() != null) {
            payment.setStripePaymentIntentId(session.getPaymentIntent());
        }

        String checkoutUrl = session.getUrl();
        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            throw new IllegalStateException("Stripe did not generate a checkout URL");
        }
        payment.setCheckoutUrl(checkoutUrl);
        payment.setShortUrl(generateShortLink(payment));
        paymentRepository.save(payment);
        return PAYMENT_MAPPER.mapEntityToResponse(payment);
    }

    @Override
    public String generateShortLink(PaymentEntity payment) {
        // Generating short link with custom base62
        // reachable through http://hostname/pay/{shortUrl}
        long value = payment.getId() * 100000 + System.currentTimeMillis() % 100000;
        return Base62.encode(BigInteger.valueOf(value));
    }

    @Override
    public PaymentResponse get(Long id) {
        return PAYMENT_MAPPER.mapEntityToResponse(paymentRepository.findById(id).orElseThrow());
    }

    @Override
    public List<PaymentResponse> history(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(PAYMENT_MAPPER::mapEntityToResponse).toList();
    }

    @Override
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll()
                .stream().map(PAYMENT_MAPPER::mapEntityToResponse).toList();
    }
}

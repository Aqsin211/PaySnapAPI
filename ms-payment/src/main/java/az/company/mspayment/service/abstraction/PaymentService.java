package az.company.mspayment.service.abstraction;

import az.company.mspayment.dao.entity.PaymentEntity;
import az.company.mspayment.model.request.PaymentRequest;
import az.company.mspayment.model.response.PaymentResponse;
import com.stripe.exception.StripeException;

import java.util.List;

public interface PaymentService {
    PaymentResponse createCheckout(Long userId, PaymentRequest paymentRequest) throws StripeException;

    String generateShortLink(PaymentEntity payment);

    PaymentResponse get(Long id);

    List<PaymentResponse> history(Long userId);

    List<PaymentResponse> getAll();
}

package az.company.mspayment.scheduler;

import az.company.mspayment.dao.repository.PaymentRepository;
import az.company.mspayment.model.enums.PaymentStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@EnableScheduling
public class PaymentScheduler {
    private final PaymentRepository paymentRepository;

    public PaymentScheduler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Scheduled(fixedDelayString = "PT5M") // every 5 minutes
    public void expireOldPending() {
        var now = OffsetDateTime.now();
        var expired = paymentRepository.findByStatusAndExpiresAtBefore(PaymentStatus.PENDING, now);
        expired.forEach(payment -> {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
        });
    }
}

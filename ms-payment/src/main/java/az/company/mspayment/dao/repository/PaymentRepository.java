package az.company.mspayment.dao.repository;

import az.company.mspayment.dao.entity.PaymentEntity;
import az.company.mspayment.model.enums.PaymentStatus;
import az.company.mspayment.util.MapperUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PaymentEntity> findByStatusAndExpiresAtBefore(PaymentStatus status, OffsetDateTime before);

    Optional<PaymentEntity> findByStripeSessionId(String id);

    Optional<PaymentEntity> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<PaymentEntity> findByShortUrl(String shortUrl);
}

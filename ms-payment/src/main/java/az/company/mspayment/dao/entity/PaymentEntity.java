package az.company.mspayment.dao.entity;


import az.company.mspayment.model.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String description;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column(columnDefinition = "text")
    private String stripePaymentIntentId;
    @Column(columnDefinition = "text")
    private String stripeSessionId;

    @Column(columnDefinition = "text")
    private String checkoutUrl;
    @Column(columnDefinition = "text")
    private String shortUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime completedAt;

    private String receiptFilePath;
}
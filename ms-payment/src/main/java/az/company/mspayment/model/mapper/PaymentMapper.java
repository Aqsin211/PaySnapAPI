package az.company.mspayment.model.mapper;

import az.company.mspayment.dao.entity.PaymentEntity;
import az.company.mspayment.model.response.PaymentResponse;

public enum PaymentMapper {
    PAYMENT_MAPPER;

    public PaymentResponse mapEntityToResponse(PaymentEntity payment) {
        return PaymentResponse.builder()
                .userId(payment.getUserId())
                .paymentId(payment.getId())
                .shortUrl(payment.getShortUrl())
                .checkoutUrl(payment.getCheckoutUrl())
                .receiptFilePath(payment.getReceiptFilePath())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .status(payment.getStatus().name())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }

}

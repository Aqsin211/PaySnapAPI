package az.company.mspayment.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private String shortUrl;
    private String checkoutUrl;
    @JsonIgnore
    private String receiptFilePath;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
}

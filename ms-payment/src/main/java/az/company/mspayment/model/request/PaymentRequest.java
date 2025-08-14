package az.company.mspayment.model.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {
    @NotNull
    @DecimalMin("0.50")
    private BigDecimal amount;
    @NotBlank
    private String currency;
    @NotBlank
    private String description;
}

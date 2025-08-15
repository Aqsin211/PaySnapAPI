package az.company.mspayment.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessages {
    NOT_LOGGED("User is not logged in"),
    FORBIDDEN("Forbidden"),
    UNAUTHORIZED("Unauthorized"),
    VALIDATION_FAILED("Validation Failed"),
    WEBHOOK_ERROR("Webhook Signature Error"),
    RESOURCE_NOT_FOUND("Resource Not Found"),
    INTERNAL_SERVER_ERROR("Internal Server Error"),
    STRIPE_API_ERROR("Stripe API Error");
    private final String message;
}
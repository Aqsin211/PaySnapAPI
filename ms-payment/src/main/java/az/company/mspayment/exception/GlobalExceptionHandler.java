package az.company.mspayment.exception;

import org.springframework.security.access.AccessDeniedException;
import com.stripe.exception.StripeException;
import com.stripe.exception.SignatureVerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static az.company.mspayment.model.enums.ErrorMessages.FORBIDDEN;
import static az.company.mspayment.model.enums.ErrorMessages.INTERNAL_SERVER_ERROR;
import static az.company.mspayment.model.enums.ErrorMessages.RESOURCE_NOT_FOUND;
import static az.company.mspayment.model.enums.ErrorMessages.STRIPE_API_ERROR;
import static az.company.mspayment.model.enums.ErrorMessages.UNAUTHORIZED;
import static az.company.mspayment.model.enums.ErrorMessages.VALIDATION_FAILED;
import static az.company.mspayment.model.enums.ErrorMessages.WEBHOOK_ERROR;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(VALIDATION_FAILED.getMessage())
                .message(errors)
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<ErrorResponse> handleStripeException(StripeException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_GATEWAY.value())
                .error(STRIPE_API_ERROR.getMessage())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(SignatureVerificationException.class)
    public ResponseEntity<ErrorResponse> handleSignatureException(SignatureVerificationException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(WEBHOOK_ERROR.getMessage())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NoSuchElementException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(RESOURCE_NOT_FOUND.getMessage())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(INTERNAL_SERVER_ERROR.getMessage())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(NotAuthenticatedException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(UNAUTHORIZED.getMessage())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error(FORBIDDEN.getMessage())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}

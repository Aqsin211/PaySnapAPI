package az.company.mspayment.exception;

public class CustomFeignException extends RuntimeException {
  public CustomFeignException(String message) {
    super(message);
  }
}

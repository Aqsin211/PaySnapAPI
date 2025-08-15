package az.company.mspayment.service.abstraction;

public interface QRCodeService {
    byte[] png(String contents, int size);
}

package az.company.mspayment.service.abstraction;

import az.company.mspayment.dao.entity.PaymentEntity;

import java.nio.file.Path;

public interface ReceiptService {
    Path generateReceipt(PaymentEntity payment);
}

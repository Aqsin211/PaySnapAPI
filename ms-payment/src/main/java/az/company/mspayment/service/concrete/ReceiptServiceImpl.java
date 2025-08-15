package az.company.mspayment.service.concrete;

import az.company.mspayment.dao.entity.PaymentEntity;
import az.company.mspayment.service.abstraction.ReceiptService;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    private final Path receiptDirectory;

    public ReceiptServiceImpl(@Value("${receipts.dir:./receipts}") String dir) throws Exception {
        this.receiptDirectory = Paths.get(dir);
        Files.createDirectories(receiptDirectory);
    }

    @Override
    public Path generateReceipt(PaymentEntity payment) {
        try {
            Path out = receiptDirectory.resolve("receipt-" + payment.getId() + ".pdf");
            try (OutputStream os = Files.newOutputStream(out)) {
                Document doc = new Document();
                PdfWriter.getInstance(doc, os);
                doc.open();
                doc.add(new Paragraph("PaySnap Receipt"));
                doc.add(new Paragraph("Order ID: " + payment.getId()));
                doc.add(new Paragraph("Amount: " + payment.getAmount() + " " + payment.getCurrency()));
                doc.add(new Paragraph("Description: " + payment.getDescription()));
                doc.add(new Paragraph("Status: " + payment.getStatus()));
                doc.add(new Paragraph("Completed At: " + payment.getCompletedAt()));
                doc.close();
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("PDF generation failed", e);
        }
    }
}

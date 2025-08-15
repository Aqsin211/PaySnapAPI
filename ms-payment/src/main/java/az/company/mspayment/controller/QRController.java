package az.company.mspayment.controller;

import az.company.mspayment.service.concrete.PaymentServiceImpl;
import az.company.mspayment.service.concrete.QRCodeServiceImpl;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/payment/qr")
public class QRController {
    private final QRCodeServiceImpl qrService;
    private final PaymentServiceImpl paymentServiceImpl;

    public QRController(QRCodeServiceImpl qrService, PaymentServiceImpl paymentServiceImpl) {
        this.qrService = qrService;
        this.paymentServiceImpl = paymentServiceImpl;
    }

    @GetMapping("/{id}/png")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public ResponseEntity<byte[]> qr(@PathVariable Long id) {
        var payment = paymentServiceImpl.get(id);
        byte[] png = qrService.png(payment.getCheckoutUrl(), 300);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    @GetMapping("/{id}/png-download")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public ResponseEntity<FileSystemResource> downloadQr(@PathVariable Long id) throws IOException {
        var payment = paymentServiceImpl.get(id);
        byte[] png = qrService.png(payment.getCheckoutUrl(), 300);

        Path tempFile = Files.createTempFile("qr-" + id, ".png");
        Files.write(tempFile, png);

        var resource = new FileSystemResource(tempFile.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qr-" + id + ".png")
                .body(resource);
    }

    @GetMapping("/{id}/pdf-download")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public ResponseEntity<FileSystemResource> downloadQrPdf(@PathVariable Long id) throws IOException, DocumentException {
        var payment = paymentServiceImpl.get(id);
        byte[] png = qrService.png(payment.getCheckoutUrl(), 300);

        Path tempFile = Files.createTempFile("qr-" + id, ".pdf");
        try (OutputStream os = Files.newOutputStream(tempFile)) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, os);
            doc.open();
            BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(png));
            Image qr = Image.getInstance(qrImage, null);
            doc.add(qr);
            doc.close();
        }

        var resource = new FileSystemResource(tempFile.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qr-" + id + ".pdf")
                .body(resource);
    }


}

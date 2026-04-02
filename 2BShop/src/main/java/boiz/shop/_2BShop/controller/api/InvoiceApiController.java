package boiz.shop._2BShop.controller.api;

import boiz.shop._2BShop.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Invoice API")
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceApiController {

    @Autowired
    private InvoiceService invoiceService;

    @Operation(summary = "Download Word invoice")
    @GetMapping("/{orderId}/word")
    public ResponseEntity<byte[]> downloadWordInvoice(@PathVariable Integer orderId) {
        try {
            byte[] content = invoiceService.generateWordInvoice(orderId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".docx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Download PDF invoice")
    @GetMapping("/{orderId}/pdf")
    public ResponseEntity<byte[]> downloadPdfInvoice(@PathVariable Integer orderId) {
        try {
            byte[] content = invoiceService.generatePdfInvoice(orderId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

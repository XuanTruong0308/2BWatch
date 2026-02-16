package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.OrderRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class InvoiceService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    private static final String FONT_PATH = "C:/Windows/Fonts/arial.ttf"; // Adjust based on OS/Server
    private static final String LOGO_PATH = "static/img/logo.png";

    // Format currency
    private String formatCurrency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }

    // Format time
    private String formatTime(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    /**
     * Generate Word Invoice (.docx)
     */
    public byte[] generateWordInvoice(Integer orderId) throws IOException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);

        try (XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Add Logo
            try {
                ClassPathResource logoResource = new ClassPathResource(LOGO_PATH);
                InputStream logoStream = logoResource.getInputStream();
                
                XWPFParagraph logoParagraph = document.createParagraph();
                logoParagraph.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun logoRun = logoParagraph.createRun();
                logoRun.addPicture(logoStream, XWPFDocument.PICTURE_TYPE_PNG, "logo.png",
                        Units.toEMU(120), Units.toEMU(60)); // Width: 120px, Height: 60px
                logoStream.close();
            } catch (Exception e) {
                System.err.println("Failed to add logo to Word invoice: " + e.getMessage());
            }

            // Title
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("HÓA ĐƠN BÁN HÀNG");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.addBreak();

            // Company Info
            XWPFParagraph company = document.createParagraph();
            company.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun companyRun = company.createRun();
            companyRun.setText("2BSHOP - LUXURY WATCHES");
            companyRun.setBold(true);
            companyRun.addBreak();
            companyRun.setText("Địa chỉ: 123 Đường Minh Khai, Hai Bà Trưng, Hà Nội");
            companyRun.addBreak();
            companyRun.setText("Hotline: 0123.456.789 | Email: contact@2bshop.com");
            companyRun.addBreak();
            companyRun.setText("Website: www.2bshop.com");

            // Order Info
            XWPFParagraph info = document.createParagraph();
            XWPFRun infoRun = info.createRun();
            infoRun.addBreak();
            infoRun.setText("══════════════════════════════════════════════");
            infoRun.addBreak();
            infoRun.setText("Mã đơn hàng: ORD" + String.format("%06d", order.getOrderId()));
            infoRun.setBold(true);
            infoRun.addBreak();
            infoRun.setText("Ngày đặt: " + formatTime(order.getOrderDate()));
            infoRun.addBreak();
            infoRun.setText("Trạng thái: " + getOrderStatusText(order.getOrderStatus()));
            infoRun.addBreak();
            infoRun.setText("══════════════════════════════════════════════");
            infoRun.addBreak();
            infoRun.addBreak();
            infoRun.setText("THÔNG TIN KHÁCH HÀNG");
            infoRun.setBold(true);
            infoRun.addBreak();
            infoRun.setText("Họ tên: " + order.getReceiverName());
            infoRun.addBreak();
            infoRun.setText("SĐT: " + order.getShippingPhone());
            infoRun.addBreak();
            infoRun.setText("Email: " + order.getUser().getEmail());
            infoRun.addBreak();
            infoRun.setText("Địa chỉ giao hàng: " + order.getShippingAddress());
            infoRun.addBreak();

            // Table
            XWPFTable table = document.createTable();
            table.setWidth("100%");

            // Header
            XWPFTableRow header = table.getRow(0);
            header.getCell(0).setText("STT");
            header.addNewTableCell().setText("Sản phẩm");
            header.addNewTableCell().setText("SL");
            header.addNewTableCell().setText("Đơn giá");
            header.addNewTableCell().setText("Thành tiền");

            // Rows
            int i = 1;
            BigDecimal subtotalBeforeVAT = BigDecimal.ZERO;
            for (OrderDetail detail : details) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(String.valueOf(i++));
                row.getCell(1).setText(detail.getWatch().getWatchName());
                row.getCell(2).setText(String.valueOf(detail.getQuantity()));
                row.getCell(3).setText(formatCurrency(detail.getUnitPrice()));
                row.getCell(4).setText(formatCurrency(detail.getSubtotal()));
                subtotalBeforeVAT = subtotalBeforeVAT.add(detail.getSubtotal());
            }

            // VAT Calculation (10%)
            BigDecimal vatAmount = subtotalBeforeVAT.multiply(new BigDecimal("0.10"));
            BigDecimal totalWithVAT = subtotalBeforeVAT.add(vatAmount);

            // Footer (Totals)
            XWPFParagraph total = document.createParagraph();
            total.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun totalRun = total.createRun();
            totalRun.addBreak();
            totalRun.setText("═══════════════════════════════════════════════");
            totalRun.addBreak();
            totalRun.setText("Tổng tiền hàng (chưa VAT): " + formatCurrency(subtotalBeforeVAT));
            totalRun.addBreak();
            totalRun.setText("Thuế VAT (10%): " + formatCurrency(vatAmount));
            totalRun.addBreak();
            totalRun.setBold(true);
            totalRun.setText("TỔNG CỘNG: " + formatCurrency(totalWithVAT));
            totalRun.addBreak();
            totalRun.setBold(false);
            if (order.getDepositAmount() != null && order.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalRun.setText("Đã thanh toán: " + formatCurrency(order.getDepositAmount()));
                totalRun.addBreak();
                totalRun.setBold(true);
                totalRun.setText("Còn lại: " + formatCurrency(order.getRemainingAmount()));
            }
            totalRun.addBreak();
            totalRun.addBreak();
            
            // Signature section
            XWPFParagraph signature = document.createParagraph();
            XWPFRun sigRun = signature.createRun();
            sigRun.addBreak();
            sigRun.setText("═══════════════════════════════════════════════");
            sigRun.addBreak();
            sigRun.addBreak();
            
            XWPFTable signTable = document.createTable(1, 2);
            signTable.setWidth("100%");
            XWPFTableRow signRow = signTable.getRow(0);
            signRow.getCell(0).setText("Người mua hàng\n\n\n(Ký, ghi rõ họ tên)");
            signRow.getCell(0).setColor("FFFFFF");
            signRow.getCell(1).setText("Người bán hàng\n\n\n(Ký, đóng dấu)");
            signRow.getCell(1).setColor("FFFFFF");
            
            XWPFParagraph footer = document.createParagraph();
            footer.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun footerRun = footer.createRun();
            footerRun.addBreak();
            footerRun.setText("Cảm ơn quý khách đã mua hàng tại 2BSHOP!");
            footerRun.setItalic(true);

            document.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Generate PDF Invoice
     */
    public byte[] generatePdfInvoice(Integer orderId) throws IOException, DocumentException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Font support for Vietnamese
            BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontTitle = new Font(bf, 18, Font.BOLD);
            Font fontHeader = new Font(bf, 12, Font.BOLD);
            Font fontNormal = new Font(bf, 11, Font.NORMAL);
            Font fontSmall = new Font(bf, 9, Font.NORMAL);

            // Add Logo
            try {
                ClassPathResource logoResource = new ClassPathResource(LOGO_PATH);
                Image logo = Image.getInstance(logoResource.getURL());
                logo.scaleToFit(120, 60); // Scale logo
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                System.err.println("Failed to add logo to PDF invoice: " + e.getMessage());
            }

            // Title
            Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Company Info
            Paragraph company = new Paragraph();
            company.add(new Phrase("2BSHOP - LUXURY WATCHES\n", fontHeader));
            company.add(new Phrase("Địa chỉ: 123 Đường Minh Khai, Hai Bà Trưng, Hà Nội\n", fontSmall));
            company.add(new Phrase("Hotline: 0123.456.789 | Email: contact@2bshop.com\n", fontSmall));
            company.add(new Phrase("Website: www.2bshop.com", fontSmall));
            company.setAlignment(Element.ALIGN_CENTER);
            company.setSpacingAfter(15);
            document.add(company);

            // Separator
            Paragraph separator = new Paragraph("═══════════════════════════════════════════════", fontNormal);
            separator.setAlignment(Element.ALIGN_CENTER);
            document.add(separator);
            document.add(new Paragraph("\n"));

            // Order Info
            document.add(new Paragraph("Mã đơn hàng: ORD" + String.format("%06d", order.getOrderId()), fontHeader));
            document.add(new Paragraph("Ngày đặt: " + formatTime(order.getOrderDate()), fontNormal));
            document.add(new Paragraph("Trạng thái: " + getOrderStatusText(order.getOrderStatus()), fontNormal));
            
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("THÔNG TIN KHÁCH HÀNG", fontHeader));
            document.add(new Paragraph("Họ tên: " + order.getReceiverName(), fontNormal));
            document.add(new Paragraph("SĐT: " + order.getShippingPhone(), fontNormal));
            document.add(new Paragraph("Email: " + order.getUser().getEmail(), fontNormal));
            document.add(new Paragraph("Địa chỉ giao hàng: " + order.getShippingAddress(), fontNormal));
            document.add(new Paragraph("\n"));

            // Table
            PdfPTable table = new PdfPTable(5); // 5 columns
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1, 4, 1, 2, 2 });

            // Headers
            addCell(table, "STT", fontHeader);
            addCell(table, "Sản phẩm", fontHeader);
            addCell(table, "SL", fontHeader);
            addCell(table, "Đơn giá", fontHeader);
            addCell(table, "Thành tiền", fontHeader);

            // Data
            int i = 1;
            BigDecimal subtotalBeforeVAT = BigDecimal.ZERO;
            for (OrderDetail detail : details) {
                addCell(table, String.valueOf(i++), fontNormal);
                addCell(table, detail.getWatch().getWatchName(), fontNormal);
                addCell(table, String.valueOf(detail.getQuantity()), fontNormal);
                addCell(table, formatCurrency(detail.getUnitPrice()), fontNormal);
                addCell(table, formatCurrency(detail.getSubtotal()), fontNormal);
                subtotalBeforeVAT = subtotalBeforeVAT.add(detail.getSubtotal());
            }

            document.add(table);
            document.add(new Paragraph("\n"));

            // VAT Calculation
            BigDecimal vatAmount = subtotalBeforeVAT.multiply(new BigDecimal("0.10"));
            BigDecimal totalWithVAT = subtotalBeforeVAT.add(vatAmount);

            // Separator
            Paragraph sep2 = new Paragraph("═══════════════════════════════════════════════", fontNormal);
            sep2.setAlignment(Element.ALIGN_CENTER);
            document.add(sep2);

            // Totals
            Paragraph subtotal = new Paragraph("Tổng tiền hàng (chưa VAT): " + formatCurrency(subtotalBeforeVAT), fontNormal);
            subtotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(subtotal);

            Paragraph vat = new Paragraph("Thuế VAT (10%): " + formatCurrency(vatAmount), fontNormal);
            vat.setAlignment(Element.ALIGN_RIGHT);
            document.add(vat);

            Paragraph total = new Paragraph("TỔNG CỘNG: " + formatCurrency(totalWithVAT), fontHeader);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            if (order.getDepositAmount() != null && order.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
                Paragraph deposit = new Paragraph("Đã thanh toán: " + formatCurrency(order.getDepositAmount()), fontNormal);
                deposit.setAlignment(Element.ALIGN_RIGHT);
                document.add(deposit);

                Paragraph remaining = new Paragraph("Còn lại: " + formatCurrency(order.getRemainingAmount()), fontHeader);
                remaining.setAlignment(Element.ALIGN_RIGHT);
                document.add(remaining);
            }

            // Signature section
            document.add(new Paragraph("\n\n"));
            Paragraph sep3 = new Paragraph("═══════════════════════════════════════════════", fontNormal);
            sep3.setAlignment(Element.ALIGN_CENTER);
            document.add(sep3);
            document.add(new Paragraph("\n"));

            PdfPTable signTable = new PdfPTable(2);
            signTable.setWidthPercentage(100);
            signTable.setWidths(new float[]{1, 1});

            PdfPCell buyerCell = new PdfPCell();
            buyerCell.setBorder(0);
            Paragraph buyerPara = new Paragraph();
            buyerPara.add(new Phrase("Người mua hàng\n\n\n\n(Ký, ghi rõ họ tên)", fontNormal));
            buyerPara.setAlignment(Element.ALIGN_CENTER);
            buyerCell.addElement(buyerPara);
            signTable.addCell(buyerCell);

            PdfPCell sellerCell = new PdfPCell();
            sellerCell.setBorder(0);
            Paragraph sellerPara = new Paragraph();
            sellerPara.add(new Phrase("Người bán hàng\n\n\n\n(Ký, đóng dấu)", fontNormal));
            sellerPara.setAlignment(Element.ALIGN_CENTER);
            sellerCell.addElement(sellerPara);
            signTable.addCell(sellerCell);

            document.add(signTable);

            // Footer
            document.add(new Paragraph("\n"));
            Paragraph footer = new Paragraph("Cảm ơn quý khách đã mua hàng tại 2BSHOP!", fontSmall);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        }
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String getOrderStatusText(String status) {
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang vận chuyển";
            case "DELIVERED": return "Đã giao hàng";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELED": return "Đã hủy";
            default: return status;
        }
    }
}

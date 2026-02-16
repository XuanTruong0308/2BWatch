package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.OrderRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

            // Title
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("HÓA ĐƠN BÁN HÀNG");
            titleRun.setBold(true);
            titleRun.setFontSize(20);

            // Company Info
            XWPFParagraph company = document.createParagraph();
            company.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun companyRun = company.createRun();
            companyRun.setText("2BSHOP - LUXURY WATCHES");
            companyRun.addBreak();
            companyRun.setText("Hotline: 0123.456.789");
            companyRun.addBreak();
            companyRun.setText("Email: contact@2bshop.com");

            // Order Info
            XWPFParagraph info = document.createParagraph();
            XWPFRun infoRun = info.createRun();
            infoRun.addBreak();
            infoRun.setText("Mã đơn hàng: #" + order.getOrderId());
            infoRun.addBreak();
            infoRun.setText("Ngày đặt: " + formatTime(order.getOrderDate()));
            infoRun.addBreak();
            infoRun.setText("Khách hàng: " + order.getReceiverName());
            infoRun.addBreak();
            infoRun.setText("SĐT: " + order.getShippingPhone());
            infoRun.addBreak();
            infoRun.setText("Địa chỉ: " + order.getShippingAddress());
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
            for (OrderDetail detail : details) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(String.valueOf(i++));
                row.getCell(1).setText(detail.getWatch().getWatchName());
                row.getCell(2).setText(String.valueOf(detail.getQuantity()));
                row.getCell(3).setText(formatCurrency(detail.getUnitPrice()));
                row.getCell(4).setText(formatCurrency(detail.getSubtotal()));
            }

            // Footer (Totals)
            XWPFParagraph total = document.createParagraph();
            total.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun totalRun = total.createRun();
            totalRun.addBreak();
            totalRun.setText("Tổng tiền hàng: " + formatCurrency(order.getTotalAmount()));
            totalRun.addBreak();
            if (order.getDepositAmount() != null && order.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalRun.setText("Đã cọc: " + formatCurrency(order.getDepositAmount()));
                totalRun.addBreak();
                totalRun.setText("Còn lại: " + formatCurrency(order.getRemainingAmount()));
            }

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
            Font fontNormal = new Font(bf, 12, Font.NORMAL);

            // Title
            Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Company
            Paragraph company = new Paragraph("2BSHOP - LUXURY WATCHES\nHotline: 0123.456.789", fontNormal);
            company.setAlignment(Element.ALIGN_CENTER);
            document.add(company);
            document.add(new Paragraph("\n"));

            // Order Info
            document.add(new Paragraph("Mã đơn hàng: #" + order.getOrderId(), fontNormal));
            document.add(new Paragraph("Ngày đặt: " + formatTime(order.getOrderDate()), fontNormal));
            document.add(new Paragraph("Khách hàng: " + order.getReceiverName(), fontNormal));
            document.add(new Paragraph("SĐT: " + order.getShippingPhone(), fontNormal));
            document.add(new Paragraph("Địa chỉ: " + order.getShippingAddress(), fontNormal));
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
            for (OrderDetail detail : details) {
                addCell(table, String.valueOf(i++), fontNormal);
                addCell(table, detail.getWatch().getWatchName(), fontNormal);
                addCell(table, String.valueOf(detail.getQuantity()), fontNormal);
                addCell(table, formatCurrency(detail.getUnitPrice()), fontNormal);
                addCell(table, formatCurrency(detail.getSubtotal()), fontNormal);
            }

            document.add(table);

            // Totals
            Paragraph total = new Paragraph("\nTổng tiền hàng: " + formatCurrency(order.getTotalAmount()), fontHeader);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            if (order.getDepositAmount() != null && order.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
                Paragraph deposit = new Paragraph("Đã cọc: " + formatCurrency(order.getDepositAmount()), fontNormal);
                deposit.setAlignment(Element.ALIGN_RIGHT);
                document.add(deposit);

                Paragraph remaining = new Paragraph("Còn lại: " + formatCurrency(order.getRemainingAmount()),
                        fontHeader);
                remaining.setAlignment(Element.ALIGN_RIGHT);
                document.add(remaining);
            }

            document.close();
            return out.toByteArray();
        }
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}

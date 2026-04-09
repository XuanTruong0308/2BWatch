package boiz.shop._2BShop.api.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import boiz.shop._2BShop.api.dto.AuthUserDto;
import boiz.shop._2BShop.api.dto.BankAccountDto;
import boiz.shop._2BShop.api.dto.BrandDto;
import boiz.shop._2BShop.api.dto.CartDto;
import boiz.shop._2BShop.api.dto.CartItemDto;
import boiz.shop._2BShop.api.dto.CategoryDto;
import boiz.shop._2BShop.api.dto.ChartSeriesDto;
import boiz.shop._2BShop.api.dto.CheckoutSummaryDto;
import boiz.shop._2BShop.api.dto.ImageDto;
import boiz.shop._2BShop.api.dto.OptionDto;
import boiz.shop._2BShop.api.dto.OrderDetailItemDto;
import boiz.shop._2BShop.api.dto.OrderDto;
import boiz.shop._2BShop.api.dto.PaymentMethodDto;
import boiz.shop._2BShop.api.dto.PaymentTransactionDto;
import boiz.shop._2BShop.api.dto.ProductCardDto;
import boiz.shop._2BShop.api.dto.ProductDetailDto;
import boiz.shop._2BShop.api.dto.UserDto;
import boiz.shop._2BShop.entity.BankAccount;
import boiz.shop._2BShop.entity.CartItem;
import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import boiz.shop._2BShop.entity.PaymentMethod;
import boiz.shop._2BShop.entity.PaymentTransaction;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.entity.WatchBrand;
import boiz.shop._2BShop.entity.WatchCategory;
import boiz.shop._2BShop.entity.WatchImage;
import boiz.shop._2BShop.service.CheckoutService;

@Component
public class ApiMapper {

    public ProductCardDto toProductCardDto(Watch watch) {
        String imageUrl = null;
        if (watch.getImages() != null && !watch.getImages().isEmpty()) {
            imageUrl = watch.getImages().stream()
                    .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                    .findFirst()
                    .or(() -> watch.getImages().stream().findFirst())
                    .map(WatchImage::getImageUrl)
                    .orElse(null);
        }
        return new ProductCardDto(
                watch.getWatchId(),
                watch.getWatchName(),
                watch.getDescription(),
                watch.getBrand() != null ? watch.getBrand().getBrandName() : null,
                watch.getCategory() != null ? watch.getCategory().getCategoryName() : null,
                imageUrl,
                watch.getPrice(),
                watch.getPriceAfterDiscount(),
                watch.getDiscountPercent(),
                watch.getStockQuantity(),
                watch.getSoldCount(),
                watch.getIsActive());
    }

    public ProductDetailDto toProductDetailDto(Watch watch, List<Watch> relatedProducts) {
        List<ImageDto> images = watch.getImages() == null
                ? List.of()
                : watch.getImages().stream().map(this::toImageDto).toList();
        return new ProductDetailDto(
                watch.getWatchId(),
                watch.getWatchName(),
                watch.getDescription(),
                watch.getBrand() != null ? watch.getBrand().getBrandName() : null,
                watch.getBrand() != null ? watch.getBrand().getBrandId() : null,
                watch.getCategory() != null ? watch.getCategory().getCategoryName() : null,
                watch.getCategory() != null ? watch.getCategory().getCategoryId() : null,
                watch.getPrice(),
                watch.getPriceAfterDiscount(),
                watch.getDiscountPercent(),
                watch.getStockQuantity(),
                watch.getSoldCount(),
                watch.getIsActive(),
                images,
                relatedProducts == null ? List.of() : relatedProducts.stream().map(this::toProductCardDto).toList());
    }

    public ImageDto toImageDto(WatchImage image) {
        return new ImageDto(image.getImageId(), image.getImageUrl(), image.getIsPrimary());
    }

    public CartItemDto toCartItemDto(CartItem item) {
        BigDecimal itemTotal = item.getWatch().getPriceAfterDiscount().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemDto(
                item.getCartItemId(),
                item.getQuantity(),
                item.getIsSelected(),
                itemTotal,
                toProductCardDto(item.getWatch()));
    }

    public CartDto toCartDto(List<CartItem> items, BigDecimal subtotal, BigDecimal shippingFee, BigDecimal total) {
        List<CartItemDto> itemDtos = items.stream().map(this::toCartItemDto).toList();
        int selectedCount = (int) items.stream().filter(item -> Boolean.TRUE.equals(item.getIsSelected())).count();
        return new CartDto(itemDtos, subtotal, shippingFee, total, items.size(), selectedCount);
    }

    public CheckoutSummaryDto toCheckoutSummaryDto(CheckoutService.CheckoutSummary summary) {
        return new CheckoutSummaryDto(
                summary.getSubtotal(),
                summary.getDiscountAmount(),
                summary.getShippingFee(),
                summary.getTotalAmount(),
                summary.getDepositAmount(),
                summary.isDepositRequired(),
                summary.getCouponCode());
    }

    public AuthUserDto toAuthUserDto(User user, boolean authenticated) {
        List<String> roles = user == null || user.getUserRoles() == null
                ? List.of()
                : user.getUserRoles().stream().map(userRole -> userRole.getRole().getRoleName()).toList();
        return new AuthUserDto(
                user != null ? user.getUserId() : null,
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getFullName() : null,
                user != null ? user.getPhone() : null,
                user != null ? user.getAddress() : null,
                user != null ? user.getAvatarUrl() : null,
                user != null ? user.getProvider() : null,
                user != null ? user.getPhoneVerified() : null,
                user != null ? user.getEmailVerified() : null,
                user != null ? user.getIsEnabled() : null,
                authenticated,
                roles.contains("ADMIN"),
                roles);
    }

    public OrderDetailItemDto toOrderDetailItemDto(OrderDetail detail) {
        return new OrderDetailItemDto(
                detail.getOrderDetailId(),
                detail.getQuantity(),
                detail.getUnitPrice(),
                detail.getDiscountAmount(),
                detail.getSubtotal(),
                toProductCardDto(detail.getWatch()));
    }

    public OrderDto toOrderDto(Order order) {
        List<OrderDetailItemDto> details = order.getOrderDetails() == null
                ? List.of()
                : order.getOrderDetails().stream().map(this::toOrderDetailItemDto).toList();
        return new OrderDto(
                order.getOrderId(),
                "ORD" + String.format("%06d", order.getOrderId()),
                order.getReceiverName(),
                order.getShippingPhone(),
                order.getShippingAddress(),
                order.getOrderStatus(),
                order.getNotes(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getDepositRequired(),
                order.getDepositAmount(),
                order.getDepositPaid(),
                order.getRemainingAmount(),
                order.getOrderDate(),
                order.getUpdatedDate(),
                toPaymentMethodDto(order.getPaymentMethod()),
                toBankAccountDto(order.getBankAccount()),
                toAuthUserDto(order.getUser(), true),
                details);
    }

    public BrandDto toBrandDto(WatchBrand brand) {
        long watchCount = brand.getWatches() == null ? 0L : brand.getWatches().size();
        return new BrandDto(
                brand.getBrandId(),
                brand.getBrandName(),
                brand.getDescription(),
                brand.getLogoUrl(),
                brand.getIsActive(),
                watchCount);
    }

    public CategoryDto toCategoryDto(WatchCategory category) {
        return new CategoryDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getDescription(),
                category.getIsActive());
    }

    public OptionDto toOptionDto(WatchBrand brand) {
        return new OptionDto(brand.getBrandId(), brand.getBrandName(), brand.getBrandName(), brand.getIsActive());
    }

    public OptionDto toOptionDto(WatchCategory category) {
        return new OptionDto(category.getCategoryId(), category.getCategoryName(), category.getCategoryName(),
                category.getIsActive());
    }

    public PaymentMethodDto toPaymentMethodDto(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        return new PaymentMethodDto(
                paymentMethod.getPaymentMethodId(),
                paymentMethod.getMethodName(),
                paymentMethod.getDescription(),
                paymentMethod.getIsActive(),
                paymentMethod.getCreatedDate(),
                paymentMethod.getUpdatedDate());
    }

    public BankAccountDto toBankAccountDto(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }
        return new BankAccountDto(
                bankAccount.getBankAccountId(),
                bankAccount.getBankName(),
                bankAccount.getBankCode(),
                bankAccount.getAccountNumber(),
                bankAccount.getAccountHolder(),
                bankAccount.getQrCodeUrl(),
                bankAccount.getIsActive(),
                bankAccount.getDisplayOrder(),
                bankAccount.getCreatedAt(),
                bankAccount.getUpdatedAt());
    }

    public UserDto toUserDto(User user, Long orderCount, BigDecimal totalSpent) {
        List<String> roles = user.getUserRoles() == null
                ? List.of()
                : user.getUserRoles().stream().map(userRole -> userRole.getRole().getRoleName()).toList();
        return new UserDto(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAddress(),
                user.getAvatarUrl(),
                user.getProvider(),
                user.getEmailVerified(),
                user.getPhoneVerified(),
                user.getIsEnabled(),
                user.getIsBanned(),
                user.getCreatedDate(),
                user.getUpdatedDate(),
                roles,
                orderCount,
                totalSpent);
    }

    public ChartSeriesDto toChartSeriesDto(Map<String, Object> chartData) {
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) chartData.getOrDefault("labels", List.of());
        @SuppressWarnings("unchecked")
        List<Number> data = (List<Number>) chartData.getOrDefault("data", List.of());
        return new ChartSeriesDto(labels, data);
    }

    public PaymentTransactionDto toPaymentTransactionDto(PaymentTransaction transaction) {
        return new PaymentTransactionDto(
                transaction.getTransactionId(),
                transaction.getTransactionCode(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getTransactionDate(),
                transaction.getResponseData(),
                toPaymentMethodDto(transaction.getPaymentMethod()),
                transaction.getOrder() != null ? transaction.getOrder().getOrderId() : null,
                transaction.getOrder() != null ? "ORD" + String.format("%06d", transaction.getOrder().getOrderId()) : null,
                transaction.getOrder() != null ? transaction.getOrder().getReceiverName() : null);
    }
}

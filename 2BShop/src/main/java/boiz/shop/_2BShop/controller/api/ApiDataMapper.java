package boiz.shop._2BShop.controller.api;

import boiz.shop._2BShop.entity.BankAccount;
import boiz.shop._2BShop.entity.CartItem;
import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import boiz.shop._2BShop.entity.PaymentMethod;
import boiz.shop._2BShop.entity.PaymentTransaction;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.entity.UserRole;
import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.entity.WatchBrand;
import boiz.shop._2BShop.entity.WatchCategory;
import boiz.shop._2BShop.entity.WatchImage;
import boiz.shop._2BShop.service.CheckoutService;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ApiDataMapper {

    private ApiDataMapper() {
    }

    public static Map<String, Object> userSummary(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (user == null) {
            return data;
        }

        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("fullName", user.getFullName());
        data.put("phone", user.getPhone());
        data.put("address", user.getAddress());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("provider", user.getProvider());
        data.put("phoneVerified", user.getPhoneVerified());
        data.put("emailVerified", user.getEmailVerified());
        data.put("isEnabled", user.getIsEnabled());
        data.put("isBanned", user.getIsBanned());
        data.put("createdDate", user.getCreatedDate());
        data.put("updatedDate", user.getUpdatedDate());

        List<String> roles = new ArrayList<>();
        if (user.getUserRoles() != null) {
            for (UserRole userRole : user.getUserRoles()) {
                if (userRole != null && userRole.getRole() != null) {
                    roles.add(userRole.getRole().getRoleName());
                }
            }
        }
        data.put("roles", roles);

        return data;
    }

    public static Map<String, Object> watchSummary(Watch watch) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (watch == null) {
            return data;
        }

        data.put("watchId", watch.getWatchId());
        data.put("watchName", watch.getWatchName());
        data.put("description", watch.getDescription());
        data.put("price", watch.getPrice());
        data.put("priceAfterDiscount", watch.getPriceAfterDiscount());
        data.put("discountPercent", watch.getDiscountPercent());
        data.put("stockQuantity", watch.getStockQuantity());
        data.put("soldCount", watch.getSoldCount());
        data.put("isActive", watch.getIsActive());
        data.put("createdDate", watch.getCreatedDate());
        data.put("updatedDate", watch.getUpdatedDate());

        if (watch.getBrand() != null) {
            data.put("brand", brand(watch.getBrand()));
        }

        if (watch.getCategory() != null) {
            data.put("category", category(watch.getCategory()));
        }

        data.put("mainImageUrl", mainImageUrl(watch));

        return data;
    }

    public static Map<String, Object> watchDetail(Watch watch) {
        Map<String, Object> data = watchSummary(watch);
        data.put("images", imageUrls(watch));
        return data;
    }

    public static List<Map<String, Object>> mapWatches(List<Watch> watches) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (watches == null) {
            return data;
        }
        for (Watch watch : watches) {
            data.add(watchSummary(watch));
        }
        return data;
    }

    public static Map<String, Object> cartItem(CartItem item) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (item == null) {
            return data;
        }

        data.put("cartItemId", item.getCartItemId());
        data.put("quantity", item.getQuantity());
        data.put("isSelected", item.getIsSelected());
        data.put("addedDate", item.getAddedDate());

        if (item.getWatch() != null) {
            Map<String, Object> watch = watchSummary(item.getWatch());
            data.put("watch", watch);
            data.put("lineSubtotal", item.getWatch().getPriceAfterDiscount().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        return data;
    }

    public static List<Map<String, Object>> mapCartItems(List<CartItem> items) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (items == null) {
            return data;
        }
        for (CartItem item : items) {
            data.add(cartItem(item));
        }
        return data;
    }

    public static Map<String, Object> checkoutSummary(CheckoutService.CheckoutSummary summary) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (summary == null) {
            return data;
        }

        data.put("subtotal", summary.getSubtotal());
        data.put("discountAmount", summary.getDiscountAmount());
        data.put("shippingFee", summary.getShippingFee());
        data.put("totalAmount", summary.getTotalAmount());
        data.put("depositAmount", summary.getDepositAmount());
        data.put("depositRequired", summary.isDepositRequired());
        data.put("couponCode", summary.getCouponCode());

        return data;
    }

    public static Map<String, Object> orderSummary(Order order) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (order == null) {
            return data;
        }

        data.put("orderId", order.getOrderId());
        data.put("orderCode", "ORD" + String.format("%06d", order.getOrderId()));
        data.put("orderStatus", order.getOrderStatus());
        data.put("orderDate", order.getOrderDate());
        data.put("updatedDate", order.getUpdatedDate());
        data.put("totalAmount", order.getTotalAmount());
        data.put("shippingAddress", order.getShippingAddress());
        data.put("shippingPhone", order.getShippingPhone());
        data.put("receiverName", order.getReceiverName());
        data.put("notes", order.getNotes());
        data.put("depositRequired", order.getDepositRequired());
        data.put("depositAmount", order.getDepositAmount());
        data.put("depositPaid", order.getDepositPaid());
        data.put("remainingAmount", order.getRemainingAmount());
        data.put("couponCode", order.getCouponCode());
        data.put("discountAmount", order.getDiscountAmount());

        if (order.getUser() != null) {
            data.put("user", userSummary(order.getUser()));
        }

        if (order.getPaymentMethod() != null) {
            data.put("paymentMethod", paymentMethod(order.getPaymentMethod()));
        }

        if (order.getBankAccount() != null) {
            data.put("bankAccount", bankAccount(order.getBankAccount()));
        }

        return data;
    }

    public static Map<String, Object> orderDetail(Order order, List<OrderDetail> details) {
        Map<String, Object> data = orderSummary(order);
        List<Map<String, Object>> detailItems = new ArrayList<>();

        if (details != null) {
            for (OrderDetail detail : details) {
                detailItems.add(orderLine(detail));
            }
        }

        data.put("orderDetails", detailItems);
        return data;
    }

    public static List<Map<String, Object>> mapOrders(List<Order> orders) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (orders == null) {
            return data;
        }
        for (Order order : orders) {
            data.add(orderSummary(order));
        }
        return data;
    }

    public static Map<String, Object> orderLine(OrderDetail detail) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (detail == null) {
            return data;
        }

        data.put("orderDetailId", detail.getOrderDetailId());
        data.put("quantity", detail.getQuantity());
        data.put("unitPrice", detail.getUnitPrice());
        data.put("discountAmount", detail.getDiscountAmount());
        data.put("subtotal", detail.getSubtotal());

        if (detail.getWatch() != null) {
            data.put("watch", watchSummary(detail.getWatch()));
        }

        return data;
    }

    public static Map<String, Object> paymentMethod(PaymentMethod method) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (method == null) {
            return data;
        }

        data.put("paymentMethodId", method.getPaymentMethodId());
        data.put("methodName", method.getMethodName());
        data.put("description", method.getDescription());
        data.put("isActive", method.getIsActive());
        data.put("createdDate", method.getCreatedDate());
        data.put("updatedDate", method.getUpdatedDate());
        return data;
    }

    public static Map<String, Object> paymentTransaction(PaymentTransaction transaction) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (transaction == null) {
            return data;
        }

        data.put("transactionId", transaction.getTransactionId());
        data.put("transactionCode", transaction.getTransactionCode());
        data.put("status", transaction.getStatus());
        data.put("amount", transaction.getAmount());
        data.put("transactionDate", transaction.getTransactionDate());
        data.put("responseData", transaction.getResponseData());

        if (transaction.getOrder() != null) {
            data.put("orderId", transaction.getOrder().getOrderId());
            data.put("orderCode", "ORD" + String.format("%06d", transaction.getOrder().getOrderId()));
            if (transaction.getOrder().getUser() != null) {
                data.put("customerEmail", transaction.getOrder().getUser().getEmail());
                data.put("customerName", transaction.getOrder().getReceiverName());
            }
        }

        if (transaction.getPaymentMethod() != null) {
            data.put("paymentMethod", paymentMethod(transaction.getPaymentMethod()));
        }

        return data;
    }

    public static List<Map<String, Object>> mapPaymentTransactions(List<PaymentTransaction> transactions) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (transactions == null) {
            return data;
        }

        for (PaymentTransaction transaction : transactions) {
            data.add(paymentTransaction(transaction));
        }

        return data;
    }

    public static Map<String, Object> bankAccount(BankAccount account) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (account == null) {
            return data;
        }

        data.put("bankAccountId", account.getBankAccountId());
        data.put("bankName", account.getBankName());
        data.put("bankCode", account.getBankCode());
        data.put("accountNumber", account.getAccountNumber());
        data.put("accountHolder", account.getAccountHolder());
        data.put("qrImageUrl", account.getQrImageUrl());
        data.put("qrCodeUrl", account.getQrCodeUrl());
        data.put("isActive", account.getIsActive());
        data.put("displayOrder", account.getDisplayOrder());
        data.put("createdAt", account.getCreatedAt());
        data.put("updatedAt", account.getUpdatedAt());

        return data;
    }

    public static List<Map<String, Object>> mapBankAccounts(List<BankAccount> accounts) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (accounts == null) {
            return data;
        }

        for (BankAccount account : accounts) {
            data.add(bankAccount(account));
        }

        return data;
    }

    public static Map<String, Object> brand(WatchBrand brand) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (brand == null) {
            return data;
        }

        data.put("brandId", brand.getBrandId());
        data.put("brandName", brand.getBrandName());
        data.put("description", brand.getDescription());
        data.put("logoUrl", brand.getLogoUrl());
        data.put("isActive", brand.getIsActive());
        data.put("watchCount", brand.getWatches() == null ? 0 : brand.getWatches().size());

        return data;
    }

    public static List<Map<String, Object>> mapBrands(List<WatchBrand> brands) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (brands == null) {
            return data;
        }
        for (WatchBrand brand : brands) {
            data.add(brand(brand));
        }
        return data;
    }

    public static Map<String, Object> category(WatchCategory category) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (category == null) {
            return data;
        }

        data.put("categoryId", category.getCategoryId());
        data.put("categoryName", category.getCategoryName());
        data.put("description", category.getDescription());
        data.put("isActive", category.getIsActive());
        data.put("watchCount", category.getWatches() == null ? 0 : category.getWatches().size());

        return data;
    }

    public static List<Map<String, Object>> mapCategories(List<WatchCategory> categories) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (categories == null) {
            return data;
        }
        for (WatchCategory category : categories) {
            data.add(category(category));
        }
        return data;
    }

    public static Map<String, Object> pageInfo(Page<?> page) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("number", page.getNumber());
        info.put("size", page.getSize());
        info.put("totalElements", page.getTotalElements());
        info.put("totalPages", page.getTotalPages());
        info.put("first", page.isFirst());
        info.put("last", page.isLast());
        return info;
    }

    private static String mainImageUrl(Watch watch) {
        if (watch == null || watch.getImages() == null) {
            return null;
        }

        for (WatchImage image : watch.getImages()) {
            if (image != null && Boolean.TRUE.equals(image.getIsPrimary())) {
                return image.getImageUrl();
            }
        }

        for (WatchImage image : watch.getImages()) {
            if (image != null && image.getImageUrl() != null) {
                return image.getImageUrl();
            }
        }

        return null;
    }

    private static List<String> imageUrls(Watch watch) {
        List<String> urls = new ArrayList<>();
        if (watch == null || watch.getImages() == null) {
            return urls;
        }

        for (WatchImage image : watch.getImages()) {
            if (image != null && image.getImageUrl() != null) {
                urls.add(image.getImageUrl());
            }
        }

        return urls;
    }
}

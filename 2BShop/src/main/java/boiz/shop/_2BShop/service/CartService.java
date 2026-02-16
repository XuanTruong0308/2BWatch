package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.*;
import boiz.shop._2BShop.respository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private WatchRepository watchRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    private boolean isUserAuthenticated() {
        try {
            org.springframework.security.core.Authentication auth = 
                SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lấy giỏ hàng của user hiện tại
     */
    private Cart getCurrentUserCart() {
        if (!isUserAuthenticated()) {
            throw new RuntimeException("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng!");
        }
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    // Thêm sản phẩm vào giỏ
    @Transactional
    public void addToCart(Integer watchId, Integer quantity) {
        Cart cart = getCurrentUserCart();

        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Kiểm tra stock
        if (watch.getStockQuantity() < quantity) {
            throw new RuntimeException("Không đủ hàng trong kho!");
        }

        // check xem có trong giỏ chưa
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartAndWatch(cart, watch);

        if (existingItem.isPresent()) {
            // cập nhật số lượng
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (watch.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Không đủ hàng trong kho!");
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Thêm mới
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setWatch(watch);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void updateQuantity(Integer cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item không tồn tại!"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            // check stock
            if (item.getWatch().getStockQuantity() < quantity) {
                throw new RuntimeException("Không đủ hàng trong kho!");
            }

            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    // xóa item khỏi giỏ
    @Transactional
    public void removeCartItem(Integer cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    // lấy danh sách cart items
    public List<CartItem> getCurrentUserCartItems() {
        Cart cart = getCurrentUserCart();
        return cartItemRepository.findByCart(cart);
    }

    // Tính subtotal (chỉ tính các item được chọn)
    public BigDecimal calculateSubtotal() {
        List<CartItem> items = getSelectedCartItems();

        return items.stream()
                .map(item -> {
                    BigDecimal price = item.getWatch().getPriceAfterDiscount();
                    return price.multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Đếm số lượng items trong giỏ
     */
    public int getCartItemCount() {
        try {
            if (!isUserAuthenticated()) {
                return 0;
            }
            return getCurrentUserCartItems().size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Update selection status of a cart item
     */
    @Transactional
    public void updateSelection(Integer cartItemId, Boolean isSelected) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setIsSelected(isSelected);
        cartItemRepository.save(item);
    }

    /**
     * Select or deselect all items in cart
     */
    @Transactional
    public void selectAll(Boolean isSelected) {
        List<CartItem> items = getCurrentUserCartItems();
        for (CartItem item : items) {
            item.setIsSelected(isSelected);
        }
        cartItemRepository.saveAll(items);
    }

    /**
     * Get only selected cart items
     */
    public List<CartItem> getSelectedCartItems() {
        Cart cart = getCurrentUserCart();
        return cartItemRepository.findByCartAndIsSelected(cart, true);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @Transactional
    public void clearCart() {
        Cart cart = getCurrentUserCart();
        cartItemRepository.deleteByCart(cart);
    }

    /**
     * Xóa sản phẩm khỏi tất cả giỏ hàng (khi xóa product hoặc hết hàng)
     */
    @Transactional
    public void removeItemsForWatch(Integer watchId) {
        cartItemRepository.deleteByWatchWatchId(watchId);
    }
}

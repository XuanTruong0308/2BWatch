package boiz.shop._2BShop.api.controller.user;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.dto.CartDto;
import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.CartAddRequest;
import boiz.shop._2BShop.api.request.CartSelectionRequest;
import boiz.shop._2BShop.api.request.CartUpdateRequest;
import boiz.shop._2BShop.api.request.SelectAllRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
public class CartApiController {

    private final CartService cartService;
    private final ApiMapper apiMapper;

    public CartApiController(CartService cartService, ApiMapper apiMapper) {
        this.cartService = cartService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ApiResponse<CartDto> getCart() {
        return ApiResponse.success(buildCartDto());
    }

    @GetMapping("/count")
    public ApiResponse<Map<String, Integer>> count() {
        return ApiResponse.success(Map.of("count", cartService.getCartItemCount()));
    }

    @PostMapping("/add")
    public ApiResponse<Map<String, Object>> addToCart(@RequestBody CartAddRequest request) {
        Integer quantity = request.quantity() == null ? 1 : request.quantity();
        cartService.addToCart(request.watchId(), quantity);
        return ApiResponse.success(
                "Đã thêm vào giỏ hàng",
                Map.of(
                        "cartItemCount", cartService.getCartItemCount(),
                        "cart", buildCartDto()));
    }

    @PostMapping("/update")
    public ApiResponse<CartDto> updateQuantity(@RequestBody CartUpdateRequest request) {
        cartService.updateQuantity(request.cartItemId(), request.quantity());
        return ApiResponse.success("Cập nhật số lượng thành công", buildCartDto());
    }

    @PostMapping("/remove")
    public ApiResponse<CartDto> removeItem(@RequestBody CartUpdateRequest request) {
        cartService.removeCartItem(request.cartItemId());
        return ApiResponse.success("Đã xóa sản phẩm khỏi giỏ hàng", buildCartDto());
    }

    @PostMapping("/select")
    public ApiResponse<CartDto> selectItem(@RequestBody CartSelectionRequest request) {
        cartService.updateSelection(request.cartItemId(), request.isSelected());
        return ApiResponse.success(buildCartDto());
    }

    @PostMapping("/select-all")
    public ApiResponse<CartDto> selectAll(@RequestBody SelectAllRequest request) {
        cartService.selectAll(request.isSelected());
        return ApiResponse.success(buildCartDto());
    }

    private CartDto buildCartDto() {
        BigDecimal subtotal = cartService.calculateSubtotal();
        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("30000");
        BigDecimal total = subtotal.add(shippingFee);
        return apiMapper.toCartDto(cartService.getCurrentUserCartItems(), subtotal, shippingFee, total);
    }
}

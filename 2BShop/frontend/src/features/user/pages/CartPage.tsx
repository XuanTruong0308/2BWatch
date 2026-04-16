import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, Cart } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { formatCurrency, getErrorMessage } from "@/lib/utils/format";

export default function CartPage() {
  const { tx } = useI18n();
  const queryClient = useQueryClient();

  const cartQuery = useQuery({
    queryKey: ["cart"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<Cart>>("/api/v1/cart");
      return response.data;
    },
  });

  const cartMutation = useMutation({
    mutationFn: async ({ endpoint, payload }: { endpoint: string; payload: unknown }) =>
      postJson<ApiResponse<Cart>>(endpoint, payload),
    onSuccess: async (response) => {
      queryClient.setQueryData(["cart"], response.data);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["cart", "count"] }),
        queryClient.invalidateQueries({ queryKey: ["checkout"] }),
      ]);
    },
  });

  if (cartQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải giỏ hàng...", "Loading cart...")} />;
  }

  if (cartQuery.isError || !cartQuery.data) {
    return <ErrorState message={tx("Không thể tải giỏ hàng lúc này.", "We could not load your cart right now.")} />;
  }

  const cart = cartQuery.data;
  const allSelected = cart.items.length > 0 && cart.items.every((item) => item.selected);
  const checkoutReady = cart.selectedItemCount > 0;

  if (cart.items.length === 0) {
    return (
      <EmptyState
        title={tx("Giỏ hàng của bạn đang trống", "Your cart is empty")}
        description={tx(
          "Khám phá bộ sưu tập mới nhất và thêm những mẫu đồng hồ phù hợp với phong cách của bạn.",
          "Explore the latest collection and add the pieces that fit your style.",
        )}
        action={
          <Link className="button button-primary" to="/watches">
            {tx("Mua ngay", "Shop now")}
          </Link>
        }
      />
    );
  }

  return (
    <div className="split-grid">
      <section className="panel">
        <div className="section-heading">
          <div>
            <span className="eyebrow">{tx("Giỏ hàng", "Cart")}</span>
            <h2>{tx("Sản phẩm đã chọn", "Your selected pieces")}</h2>
            <p className="muted-copy">
              {tx(
                `${cart.totalItemCount} sản phẩm đang được lưu trong phiên mua sắm của bạn.`,
                `${cart.totalItemCount} product(s) are currently saved in your buying session.`,
              )}
            </p>
          </div>
          <button
            className="button button-subtle"
            onClick={() =>
              cartMutation.mutate({
                endpoint: "/api/v1/cart/select-all",
                payload: { isSelected: !allSelected },
              })
            }
            type="button"
          >
            {allSelected ? tx("Bỏ chọn tất cả", "Clear selection") : tx("Chọn tất cả", "Select all")}
          </button>
        </div>

        <div className="cart-list">
          {cart.items.map((item) => (
            <article className="cart-line" key={item.cartItemId}>
              <label className="cart-line__check">
                <input
                  checked={Boolean(item.selected)}
                  onChange={(event) =>
                    cartMutation.mutate({
                      endpoint: "/api/v1/cart/select",
                      payload: { cartItemId: item.cartItemId, isSelected: event.target.checked },
                    })
                  }
                  type="checkbox"
                />
              </label>

              <Link className="cart-line__media" to={`/watches/${item.watch.watchId}`}>
                {item.watch.imageUrl ? <img alt={item.watch.watchName} src={item.watch.imageUrl} /> : <span>2B</span>}
              </Link>

              <div className="cart-line__body">
                <div className="eyebrow">{item.watch.brandName || "2BShop"}</div>
                <h3>{item.watch.watchName}</h3>
                <p className="muted-copy">
                  {item.watch.description ||
                    tx(
                      "Thiết kế đồng hồ cao cấp được trình bày theo giao diện mua sắm mới ưu tiên sản phẩm.",
                      "A premium watch silhouette presented through the new product-first storefront.",
                    )}
                </p>
                <div className="price-stack">
                  <span className="price-main">{formatCurrency(item.watch.priceAfterDiscount)}</span>
                  {item.watch.discountPercent ? <span className="price-old">{formatCurrency(item.watch.price)}</span> : null}
                </div>
                <div className="header-actions" style={{ justifyContent: "flex-start", flexWrap: "wrap" }}>
                  <Badge
                    label={item.watch.stockQuantity && item.watch.stockQuantity > 0 ? tx("Còn hàng", "In stock") : tx("Hết hàng", "Sold out")}
                  />
                  {item.watch.discountPercent ? <Badge label={`-${item.watch.discountPercent}%`} tone="warning" /> : null}
                </div>
              </div>

              <div className="cart-line__actions">
                <div className="quantity-stepper">
                  <button
                    className="button button-subtle"
                    disabled={cartMutation.isPending}
                    onClick={() =>
                      cartMutation.mutate({
                        endpoint: "/api/v1/cart/update",
                        payload: { cartItemId: item.cartItemId, quantity: Math.max(1, item.quantity - 1) },
                      })
                    }
                    type="button"
                  >
                    -
                  </button>
                  <span>{item.quantity}</span>
                  <button
                    className="button button-subtle"
                    disabled={cartMutation.isPending}
                    onClick={() =>
                      cartMutation.mutate({
                        endpoint: "/api/v1/cart/update",
                        payload: { cartItemId: item.cartItemId, quantity: item.quantity + 1 },
                      })
                    }
                    type="button"
                  >
                    +
                  </button>
                </div>

                <strong>{formatCurrency(item.itemTotal)}</strong>

                <button
                  className="button button-danger"
                  disabled={cartMutation.isPending}
                  onClick={() =>
                    cartMutation.mutate({
                      endpoint: "/api/v1/cart/remove",
                      payload: { cartItemId: item.cartItemId, quantity: item.quantity },
                    })
                  }
                  type="button"
                >
                  {tx("Xóa", "Remove")}
                </button>
              </div>
            </article>
          ))}
        </div>

        {cartMutation.isError ? (
          <p className="inline-text-error">{getErrorMessage(cartMutation.error, tx("Không thể cập nhật giỏ hàng.", "We could not update the cart."))}</p>
        ) : null}
      </section>

      <aside className="panel">
        <span className="eyebrow">{tx("Tóm tắt", "Summary")}</span>
        <h2>{tx("Tổng quan đơn hàng", "Order overview")}</h2>
        <div className="summary-list">
          <div className="summary-row">
            <span>{tx("Sản phẩm đã chọn", "Selected products")}</span>
            <strong>{cart.selectedItemCount}</strong>
          </div>
          <div className="summary-row">
            <span>{tx("Tạm tính", "Subtotal")}</span>
            <strong>{formatCurrency(cart.subtotal)}</strong>
          </div>
          <div className="summary-row">
            <span>{tx("Vận chuyển", "Shipping")}</span>
            <strong>{cart.shippingFee === 0 ? tx("Miễn phí", "Free") : formatCurrency(cart.shippingFee)}</strong>
          </div>
          <div className="summary-row summary-row-total">
            <span>{tx("Tổng cộng", "Total")}</span>
            <strong>{formatCurrency(cart.total)}</strong>
          </div>
        </div>

        {!checkoutReady ? (
          <div className="inline-alert inline-alert-warning" style={{ marginTop: 18 }}>
            {tx("Hãy chọn ít nhất một sản phẩm trước khi tiếp tục thanh toán.", "Select at least one product before continuing to checkout.")}
          </div>
        ) : null}

        <div className="stack-actions">
          {checkoutReady ? (
            <Link className="button button-primary" to="/checkout">
              {tx("Tiếp tục thanh toán", "Continue to checkout")}
            </Link>
          ) : (
            <button className="button button-subtle" disabled type="button">
              {tx("Chọn sản phẩm để tiếp tục", "Select products to continue")}
            </button>
          )}
          <Link className="button button-subtle" to="/watches">
            {tx("Tiếp tục mua sắm", "Continue shopping")}
          </Link>
        </div>
      </aside>
    </div>
  );
}

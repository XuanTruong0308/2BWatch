import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, Cart } from "@/lib/api/types";
import { formatCurrency, getErrorMessage } from "@/lib/utils/format";

export default function CartPage() {
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
    return <LoadingScreen label="Dang tai gio hang..." />;
  }

  if (cartQuery.isError || !cartQuery.data) {
    return <ErrorState message="Khong the tai gio hang cua ban luc nay." />;
  }

  const cart = cartQuery.data;
  const allSelected = cart.items.length > 0 && cart.items.every((item) => item.selected);
  const checkoutReady = cart.selectedItemCount > 0;

  if (cart.items.length === 0) {
    return (
      <EmptyState
        title="Gio hang cua ban dang trong"
        description="Kham pha bo suu tap moi de them nhung mau dong ho phu hop voi phong cach cua ban."
        action={
          <Link className="button button-primary" to="/watches">
            Mua sam ngay
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
            <span className="eyebrow">Cart</span>
            <h2>Gio hang ca nhan</h2>
            <p>{cart.totalItemCount} san pham dang duoc giu trong phien mua sam cua ban.</p>
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
            {allSelected ? "Bo chon tat ca" : "Chon tat ca"}
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
                <p className="muted-copy">{item.watch.description || "Thiet ke premium voi mat so hien dai."}</p>
                <div className="price-stack">
                  <span className="price-main">{formatCurrency(item.watch.priceAfterDiscount)}</span>
                  {item.watch.discountPercent ? <span className="price-old">{formatCurrency(item.watch.price)}</span> : null}
                </div>
                <div className="header-actions" style={{ justifyContent: "flex-start" }}>
                  <Badge label={item.watch.stockQuantity && item.watch.stockQuantity > 0 ? "Con hang" : "Het hang"} />
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
                  Xoa
                </button>
              </div>
            </article>
          ))}
        </div>

        {cartMutation.isError ? (
          <p className="inline-text-error">{getErrorMessage(cartMutation.error, "Khong the cap nhat gio hang.")}</p>
        ) : null}
      </section>

      <aside className="panel">
        <span className="eyebrow">Summary</span>
        <h2>Tong quan don hang</h2>
        <div className="summary-list">
          <div className="summary-row">
            <span>San pham da chon</span>
            <strong>{cart.selectedItemCount}</strong>
          </div>
          <div className="summary-row">
            <span>Tam tinh</span>
            <strong>{formatCurrency(cart.subtotal)}</strong>
          </div>
          <div className="summary-row">
            <span>Phi van chuyen</span>
            <strong>{cart.shippingFee === 0 ? "Mien phi" : formatCurrency(cart.shippingFee)}</strong>
          </div>
          <div className="summary-row summary-row-total">
            <span>Tong thanh toan</span>
            <strong>{formatCurrency(cart.total)}</strong>
          </div>
        </div>

        {!checkoutReady ? (
          <div className="inline-alert inline-alert-warning" style={{ marginTop: 18 }}>
            Hay chon it nhat mot san pham truoc khi chuyen sang thanh toan.
          </div>
        ) : null}

        <div className="stack-actions">
          {checkoutReady ? (
            <Link className="button button-primary" to="/checkout">
              Tien hanh thanh toan
            </Link>
          ) : (
            <button className="button button-subtle" disabled type="button">
              Chon san pham de thanh toan
            </button>
          )}
          <Link className="button button-subtle" to="/watches">
            Tiep tuc mua sam
          </Link>
        </div>
      </aside>
    </div>
  );
}

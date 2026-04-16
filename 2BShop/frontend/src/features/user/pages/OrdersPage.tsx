import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, Order, PaginatedResponse } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { formatCurrency, formatDate, getErrorMessage, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function OrdersPage() {
  const { tx } = useI18n();
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Number(searchParams.get("page") || 0);
  const status = searchParams.get("status") || "";

  const statusFilters = [
    { value: "", label: tx("Tất cả", "All") },
    { value: "PENDING", label: orderStatusLabel("PENDING") },
    { value: "CONFIRMED", label: orderStatusLabel("CONFIRMED") },
    { value: "SHIPPING", label: orderStatusLabel("SHIPPING") },
    { value: "DELIVERED", label: orderStatusLabel("DELIVERED") },
    { value: "COMPLETED", label: orderStatusLabel("COMPLETED") },
    { value: "CANCELLED", label: orderStatusLabel("CANCELLED") },
  ];

  const ordersQuery = useQuery({
    queryKey: ["orders", page, status],
    queryFn: async () => {
      const params = new URLSearchParams();
      params.set("page", String(page));
      params.set("size", "10");
      if (status) {
        params.set("status", status);
      }
      return getJson<PaginatedResponse<Order>>(`/api/v1/orders?${params.toString()}`);
    },
  });

  const cancelMutation = useMutation({
    mutationFn: async ({ orderId, reason }: { orderId: number; reason?: string }) =>
      postJson<ApiResponse<Order>>(`/api/v1/orders/${orderId}/cancel`, { reason }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
  });

  if (ordersQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải đơn hàng...", "Loading orders...")} />;
  }

  if (ordersQuery.isError || !ordersQuery.data) {
    return <ErrorState message={tx("Không thể tải đơn hàng của bạn.", "We could not load your orders.")} />;
  }

  const orders = ordersQuery.data;

  if (orders.items.length === 0) {
    return (
      <EmptyState
        title={tx("Chưa có đơn hàng phù hợp", "No matching orders yet")}
        description={tx(
          "Hoàn tất một lần thanh toán và lịch sử đơn hàng sẽ xuất hiện tại đây để bạn theo dõi.",
          "Complete a checkout and your order timeline will appear here for tracking.",
        )}
        action={
          <Link className="button button-primary" to="/watches">
            {tx("Khám phá bộ sưu tập", "Explore the collection")}
          </Link>
        }
      />
    );
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Đơn hàng của tôi", "My orders")}</span>
          <h2>{tx("Lịch sử mua sắm", "Your purchase history")}</h2>
          <p className="muted-copy">
            {tx(
              "Trạng thái đơn hàng luôn được đồng bộ với quy trình xử lý backend và cập nhật từ admin.",
              "Order states stay synchronized with the existing backend processing flow and admin updates.",
            )}
          </p>
        </div>
      </div>

      <div className="filter-chip-row">
        {statusFilters.map((filter) => (
          <button
            className={`button ${filter.value === status ? "button-primary" : "button-subtle"}`}
            key={filter.value || "all"}
            onClick={() => {
              const next = new URLSearchParams(searchParams);
              if (filter.value) {
                next.set("status", filter.value);
              } else {
                next.delete("status");
              }
              next.delete("page");
              setSearchParams(next);
            }}
            type="button"
          >
            {filter.label}
          </button>
        ))}
      </div>

      <div className="order-card-list">
        {orders.items.map((order) => (
          <article className="order-card-modern" key={order.orderId}>
            <div className="order-card-modern__head">
              <div>
                <div className="eyebrow">{order.orderCode}</div>
                <h3>{order.receiverName}</h3>
              </div>
              <Badge label={orderStatusLabel(order.orderStatus)} tone={statusTone(order.orderStatus)} />
            </div>

            <div className="info-grid">
              <div className="metric-card">
                <span className="eyebrow">{tx("Ngày đặt", "Order date")}</span>
                <strong>{formatDate(order.orderDate)}</strong>
              </div>
              <div className="metric-card">
                <span className="eyebrow">{tx("Thanh toán", "Payment")}</span>
                <strong>{order.paymentMethod?.methodName || tx("Không có", "N/A")}</strong>
              </div>
              <div className="metric-card">
                <span className="eyebrow">{tx("Tổng tiền", "Total")}</span>
                <strong>{formatCurrency(order.totalAmount)}</strong>
              </div>
            </div>

            <div className="header-actions" style={{ justifyContent: "flex-end", marginTop: 24, paddingTop: 24, borderTop: "1px solid var(--line)" }}>
              {["PENDING", "CONFIRMED"].includes(order.orderStatus) ? (
                <button
                  className="button button-danger"
                  disabled={cancelMutation.isPending}
                  onClick={() =>
                    cancelMutation.mutate({
                      orderId: order.orderId,
                      reason: tx("Khách hàng hủy từ lịch sử đơn hàng", "Customer cancelled from order history"),
                    })
                  }
                  type="button"
                >
                  {tx("Hủy đơn", "Cancel order")}
                </button>
              ) : null}

              <Link className="button button-subtle" to={`/my-orders/${order.orderId}`}>
                {tx("Xem chi tiết", "View details")}
              </Link>
            </div>
          </article>
        ))}
      </div>

      {cancelMutation.isError ? (
        <p className="inline-text-error">{getErrorMessage(cancelMutation.error, tx("Không thể hủy đơn hàng.", "We could not cancel the order."))}</p>
      ) : null}

      <Pagination
        currentPage={page}
        onPageChange={(nextPage) => {
          const next = new URLSearchParams(searchParams);
          next.set("page", String(nextPage));
          setSearchParams(next);
        }}
        totalPages={orders.totalPages}
      />
    </div>
  );
}

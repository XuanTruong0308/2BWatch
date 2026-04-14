import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { EmptyState } from "@/components/ui/EmptyState";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, Order, PaginatedResponse } from "@/lib/api/types";
import { formatCurrency, formatDate, getErrorMessage, orderStatusLabel, statusTone } from "@/lib/utils/format";

const statusFilters = [
  { value: "", label: "Tất cả" },
  { value: "PENDING", label: "Chờ xử lý" },
  { value: "CONFIRMED", label: "Đã xác nhận" },
  { value: "SHIPPING", label: "Đang giao" },
  { value: "DELIVERED", label: "Đã giao" },
  { value: "COMPLETED", label: "Hoàn thành" },
  { value: "CANCELLED", label: "Đã hủy" },
];

export default function OrdersPage() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Number(searchParams.get("page") || 0);
  const status = searchParams.get("status") || "";

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
    return <LoadingScreen label="Đang tải đơn hàng..." />;
  }

  if (ordersQuery.isError || !ordersQuery.data) {
    return <ErrorState message="Không thể tải danh sách đơn hàng." />;
  }

  const orders = ordersQuery.data;

  if (orders.items.length === 0) {
    return (
      <EmptyState
        title="Chưa có đơn hàng phù hợp"
        description="Khi bạn hoàn tất checkout, đơn hàng sẽ xuất hiện tại đây để tiện theo dõi."
        action={
          <Link className="button button-primary" to="/watches">
            Khám phá sản phẩm
          </Link>
        }
      />
    );
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">My Orders</span>
          <h2>Lịch sử mua sắm của bạn</h2>
          <p>Trạng thái đơn được đồng bộ trực tiếp với hệ quản trị và luồng xử lý đơn hiện tại.</p>
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
                <span className="eyebrow">Ngày đặt</span>
                <strong>{formatDate(order.orderDate)}</strong>
              </div>
              <div className="metric-card">
                <span className="eyebrow">Thanh toán</span>
                <strong>{order.paymentMethod?.methodName || "N/A"}</strong>
              </div>
              <div className="metric-card">
                <span className="eyebrow">Tổng tiền</span>
                <strong>{formatCurrency(order.totalAmount)}</strong>
              </div>
            </div>
            <div className="header-actions" style={{ justifyContent: "space-between", marginTop: "1.25rem" }}>
              <Link className="button button-subtle" to={`/my-orders/${order.orderId}`}>
                Xem chi tiết
              </Link>
              {["PENDING", "CONFIRMED"].includes(order.orderStatus) ? (
                <button
                  className="button button-danger"
                  disabled={cancelMutation.isPending}
                  onClick={() => cancelMutation.mutate({ orderId: order.orderId, reason: "Khách hàng yêu cầu hủy từ giao diện React" })}
                  type="button"
                >
                  Hủy đơn
                </button>
              ) : null}
            </div>
          </article>
        ))}
      </div>

      {cancelMutation.isError ? <p className="inline-text-error">{getErrorMessage(cancelMutation.error)}</p> : null}

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

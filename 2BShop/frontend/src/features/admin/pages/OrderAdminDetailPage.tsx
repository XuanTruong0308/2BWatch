import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson } from "@/lib/api/client";
import type { AdminOrderDetailPayload, ApiResponse } from "@/lib/api/types";
import { formatCurrency, formatDate, getErrorMessage, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function OrderAdminDetailPage() {
  const { id } = useParams();
  const queryClient = useQueryClient();

  const detailQuery = useQuery({
    queryKey: ["admin", "order-detail", id],
    queryFn: async () => {
      const response = await getJson<ApiResponse<AdminOrderDetailPayload>>(`/api/v1/admin/orders/${id}`);
      return response.data;
    },
  });

  const actionMutation = useMutation({
    mutationFn: async ({ endpoint, payload }: { endpoint: string; payload: unknown }) => postJson(endpoint, payload),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["admin", "order-detail", id] }),
        queryClient.invalidateQueries({ queryKey: ["admin", "orders"] }),
      ]);
    },
  });

  if (detailQuery.isLoading) {
    return <LoadingScreen label="Đang tải chi tiết đơn hàng..." />;
  }

  if (detailQuery.isError || !detailQuery.data) {
    return <ErrorState message="Không thể tải chi tiết đơn hàng quản trị." />;
  }

  const { order, validStatuses } = detailQuery.data;

  return (
    <div className="stack-section">
      <div className="panel">
        <div className="section-heading">
          <div>
            <Link className="muted-copy" to="/admin/orders">
              ← Quay lại danh sách
            </Link>
            <div className="eyebrow" style={{ marginTop: 12 }}>
              {order.orderCode}
            </div>
            <h2>{order.receiverName}</h2>
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
            <span className="eyebrow">Số điện thoại</span>
            <strong>{order.shippingPhone}</strong>
          </div>
          <div className="metric-card">
            <span className="eyebrow">Tổng tiền</span>
            <strong>{formatCurrency(order.totalAmount)}</strong>
          </div>
        </div>
      </div>

      <div className="panel">
        <h3>Cập nhật trạng thái</h3>
        <div className="header-actions" style={{ justifyContent: "flex-start", flexWrap: "wrap" }}>
          {validStatuses.map((status) => (
            <button
              className="button button-primary"
              key={status}
              onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/orders/${order.orderId}/status`, payload: { newStatus: status, note: "" } })}
              type="button"
            >
              Chuyển sang {orderStatusLabel(status)}
            </button>
          ))}
          {["PENDING", "CONFIRMED"].includes(order.orderStatus) ? (
            <button
              className="button button-danger"
              onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/orders/${order.orderId}/cancel`, payload: { reason: "Admin hủy đơn từ backoffice React" } })}
              type="button"
            >
              Hủy đơn
            </button>
          ) : null}
        </div>
        {actionMutation.isError ? <p className="inline-text-error">{getErrorMessage(actionMutation.error)}</p> : null}
      </div>

      <div className="panel">
        <h3>Thông tin giao hàng</h3>
        <p>
          <strong>Địa chỉ:</strong> {order.shippingAddress}
        </p>
        {order.notes ? (
          <p>
            <strong>Ghi chú:</strong> {order.notes}
          </p>
        ) : null}
      </div>

      <div className="panel">
        <h3>Sản phẩm trong đơn</h3>
        <div className="summary-list">
          {order.orderDetails.map((detail) => (
            <div className="summary-product" key={detail.orderDetailId}>
              <div>
                <strong>{detail.watch.watchName}</strong>
                <p className="muted-copy">
                  {detail.watch.brandName} · x{detail.quantity}
                </p>
              </div>
              <strong>{formatCurrency(detail.subtotal)}</strong>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

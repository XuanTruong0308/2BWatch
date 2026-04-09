import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, Order } from "@/lib/api/types";
import { formatCurrency, formatDate, getErrorMessage, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function OrderDetailPage() {
  const { orderId } = useParams();
  const queryClient = useQueryClient();

  const orderQuery = useQuery({
    queryKey: ["order", orderId],
    queryFn: async () => {
      const response = await getJson<ApiResponse<Order>>(`/api/v1/orders/${orderId}`);
      return response.data;
    },
  });

  const cancelMutation = useMutation({
    mutationFn: async () => postJson<ApiResponse<Order>>(`/api/v1/orders/${orderId}/cancel`, { reason: "Khách hàng hủy đơn từ trang chi tiết" }),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["orders"] }),
        queryClient.invalidateQueries({ queryKey: ["order", orderId] }),
      ]);
    },
  });

  if (orderQuery.isLoading) {
    return <LoadingScreen label="Đang tải chi tiết đơn hàng..." />;
  }

  if (orderQuery.isError || !orderQuery.data) {
    return <ErrorState message="Không thể tải chi tiết đơn hàng." />;
  }

  const order = orderQuery.data;
  const timelineOrder = ["PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "COMPLETED"];
  const activeIndex = timelineOrder.indexOf(order.orderStatus);

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <Link className="muted-copy" to="/my-orders">
            ← Quay lại danh sách đơn
          </Link>
          <div className="eyebrow" style={{ marginTop: 12 }}>
            {order.orderCode}
          </div>
          <h2>Đơn hàng của {order.receiverName}</h2>
        </div>
        <Badge label={orderStatusLabel(order.orderStatus)} tone={statusTone(order.orderStatus)} />
      </div>

      <div className="info-grid">
        <div className="metric-card">
          <span className="eyebrow">Ngày đặt</span>
          <strong>{formatDate(order.orderDate)}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Số điện thoại</span>
          <strong>{order.shippingPhone}</strong>
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

      <div className="panel inner-panel" style={{ marginTop: 24 }}>
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

      <div className="panel inner-panel" style={{ marginTop: 24 }}>
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

      <div className="timeline-modern" style={{ marginTop: 24 }}>
        {timelineOrder.map((status, index) => (
          <div className={`timeline-modern__item ${activeIndex >= index ? "is-active" : ""}`} key={status}>
            <strong>{orderStatusLabel(status)}</strong>
          </div>
        ))}
      </div>

      <div className="header-actions" style={{ justifyContent: "space-between", marginTop: 24 }}>
        <a className="button button-subtle" href={`/invoice/${order.orderId}/pdf`}>
          Tải hóa đơn
        </a>
        {["PENDING", "CONFIRMED"].includes(order.orderStatus) ? (
          <button className="button button-danger" disabled={cancelMutation.isPending} onClick={() => cancelMutation.mutate()} type="button">
            Hủy đơn hàng
          </button>
        ) : null}
      </div>

      {cancelMutation.isError ? <p className="inline-text-error">{getErrorMessage(cancelMutation.error)}</p> : null}
    </div>
  );
}

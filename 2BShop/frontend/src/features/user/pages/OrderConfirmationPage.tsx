import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, Order } from "@/lib/api/types";
import { formatCurrency, formatDate, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function OrderConfirmationPage() {
  const { orderId } = useParams();
  const orderQuery = useQuery({
    queryKey: ["checkout", "confirmation", orderId],
    queryFn: async () => {
      const response = await getJson<ApiResponse<Order>>(`/api/v1/checkout/confirmation/${orderId}`);
      return response.data;
    },
  });

  if (orderQuery.isLoading) {
    return <LoadingScreen label="Đang tải xác nhận đơn hàng..." />;
  }

  if (orderQuery.isError || !orderQuery.data) {
    return <ErrorState message="Không thể tải thông tin xác nhận đơn hàng." />;
  }

  const order = orderQuery.data;

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Order Confirmed</span>
          <h2>Đơn hàng {order.orderCode} đã được tạo thành công</h2>
          <p>Chúng tôi đã giữ nguyên logic tạo đơn hiện tại và chỉ làm mới lại cách hiển thị xác nhận.</p>
        </div>
        <Badge label={orderStatusLabel(order.orderStatus)} tone={statusTone(order.orderStatus)} />
      </div>

      <div className="info-grid">
        <div className="metric-card">
          <span className="eyebrow">Ngày đặt</span>
          <strong>{formatDate(order.orderDate)}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Người nhận</span>
          <strong>{order.receiverName}</strong>
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

      <div className="header-actions" style={{ marginTop: 24, justifyContent: "flex-start" }}>
        <Link className="button button-primary" to={`/my-orders/${order.orderId}`}>
          Theo dõi đơn hàng
        </Link>
        <a className="button button-subtle" href={`/invoice/${order.orderId}/pdf`}>
          Tải hóa đơn PDF
        </a>
      </div>
    </div>
  );
}

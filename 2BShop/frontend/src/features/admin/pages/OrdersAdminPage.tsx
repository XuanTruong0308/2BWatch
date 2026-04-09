import { useQuery } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { getJson } from "@/lib/api/client";
import type { AdminOrderListPayload, ApiResponse } from "@/lib/api/types";
import { formatCurrency, formatDate, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function OrdersAdminPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Number(searchParams.get("page") || 0);

  const ordersQuery = useQuery({
    queryKey: ["admin", "orders", searchParams.toString()],
    queryFn: async () => {
      const response = await getJson<ApiResponse<AdminOrderListPayload>>(`/api/v1/admin/orders?${searchParams.toString()}`);
      return response.data;
    },
  });

  if (ordersQuery.isLoading) {
    return <LoadingScreen label="Đang tải đơn hàng quản trị..." />;
  }

  if (ordersQuery.isError || !ordersQuery.data) {
    return <ErrorState message="Không thể tải danh sách đơn hàng quản trị." />;
  }

  const payload = ordersQuery.data;

  const updateSearch = (name: string, value: string) => {
    const next = new URLSearchParams(searchParams);
    if (value) {
      next.set(name, value);
    } else {
      next.delete(name);
    }
    next.delete("page");
    setSearchParams(next);
  };

  return (
    <div className="stack-section">
      <div className="metrics-grid">
        <div className="metric-card">
          <span className="eyebrow">Total</span>
          <strong>{payload.stats.totalOrders}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Pending</span>
          <strong>{payload.stats.pendingCount}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Shipping</span>
          <strong>{payload.stats.shippingCount}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Delivered</span>
          <strong>{payload.stats.deliveredCount}</strong>
        </div>
      </div>

      <div className="panel">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Orders</span>
            <h2>Điều phối đơn hàng</h2>
          </div>
        </div>

        <div className="form-grid">
          <div className="field-group">
            <label htmlFor="keyword">Tìm kiếm</label>
            <input
              className="field"
              defaultValue={searchParams.get("keyword") || ""}
              id="keyword"
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  updateSearch("keyword", (event.target as HTMLInputElement).value);
                }
              }}
              placeholder="Tên, SĐT, mã đơn..."
            />
          </div>
          <div className="field-group">
            <label htmlFor="status">Trạng thái</label>
            <select className="select" id="status" onChange={(event) => updateSearch("status", event.target.value)} value={searchParams.get("status") || ""}>
              <option value="">Tất cả</option>
              {["PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"].map((status) => (
                <option key={status} value={status}>
                  {orderStatusLabel(status)}
                </option>
              ))}
            </select>
          </div>
          <div className="field-group">
            <label htmlFor="fromDate">Từ ngày</label>
            <input className="field" id="fromDate" onChange={(event) => updateSearch("fromDate", event.target.value)} type="date" value={searchParams.get("fromDate") || ""} />
          </div>
          <div className="field-group">
            <label htmlFor="toDate">Đến ngày</label>
            <input className="field" id="toDate" onChange={(event) => updateSearch("toDate", event.target.value)} type="date" value={searchParams.get("toDate") || ""} />
          </div>
        </div>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Khách hàng</th>
              <th>Ngày đặt</th>
              <th>Thanh toán</th>
              <th>Tổng tiền</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {payload.orders.items.map((order) => (
              <tr key={order.orderId}>
                <td>
                  <strong>{order.orderCode}</strong>
                </td>
                <td>
                  {order.receiverName}
                  <div className="muted-copy">{order.shippingPhone}</div>
                </td>
                <td>{formatDate(order.orderDate)}</td>
                <td>{order.paymentMethod?.methodName || "N/A"}</td>
                <td>{formatCurrency(order.totalAmount)}</td>
                <td>
                  <Badge label={orderStatusLabel(order.orderStatus)} tone={statusTone(order.orderStatus)} />
                </td>
                <td>
                  <Link className="button button-subtle" to={`/admin/orders/${order.orderId}`}>
                    Chi tiết
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Pagination
        currentPage={page}
        onPageChange={(nextPage) => {
          const next = new URLSearchParams(searchParams);
          next.set("page", String(nextPage));
          setSearchParams(next);
        }}
        totalPages={payload.orders.totalPages}
      />
    </div>
  );
}

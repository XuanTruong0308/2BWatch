import { useQuery } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { getJson } from "@/lib/api/client";
import type { AdminOrderListPayload, ApiResponse } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { formatCurrency, formatDate, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function OrdersAdminPage() {
  const { tx } = useI18n();
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
    return <LoadingScreen label={tx("Đang tải đơn hàng quản trị...", "Loading admin orders...")} />;
  }

  if (ordersQuery.isError || !ordersQuery.data) {
    return <ErrorState message={tx("Không thể tải danh sách đơn hàng quản trị.", "Could not load admin orders.")} />;
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
          <span className="eyebrow">{tx("Tổng số", "Total")}</span>
          <strong>{payload.stats.totalOrders}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{orderStatusLabel("PENDING")}</span>
          <strong>{payload.stats.pendingCount}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{orderStatusLabel("SHIPPING")}</span>
          <strong>{payload.stats.shippingCount}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{orderStatusLabel("DELIVERED")}</span>
          <strong>{payload.stats.deliveredCount}</strong>
        </div>
      </div>

      <div className="panel">
        <div className="section-heading">
          <div>
            <span className="eyebrow">{tx("Đơn hàng", "Orders")}</span>
            <h2>{tx("Điều phối đơn hàng", "Order operations")}</h2>
          </div>
        </div>

        <div className="form-grid">
          <div className="field-group">
            <label htmlFor="keyword">{tx("Tìm kiếm", "Search")}</label>
            <input
              className="field"
              defaultValue={searchParams.get("keyword") || ""}
              id="keyword"
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  updateSearch("keyword", (event.target as HTMLInputElement).value);
                }
              }}
              placeholder={tx("Ten, SDT, ma don...", "Name, phone, order code...")}
            />
          </div>
          <div className="field-group">
            <label htmlFor="status">{tx("Trạng thái", "Status")}</label>
            <select className="select" id="status" onChange={(event) => updateSearch("status", event.target.value)} value={searchParams.get("status") || ""}>
              <option value="">{tx("Tất cả", "All")}</option>
              {["PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"].map((status) => (
                <option key={status} value={status}>
                  {orderStatusLabel(status)}
                </option>
              ))}
            </select>
          </div>
          <div className="field-group">
            <label htmlFor="fromDate">{tx("Từ ngày", "From date")}</label>
            <input className="field" id="fromDate" onChange={(event) => updateSearch("fromDate", event.target.value)} type="date" value={searchParams.get("fromDate") || ""} />
          </div>
          <div className="field-group">
            <label htmlFor="toDate">{tx("Đến ngày", "To date")}</label>
            <input className="field" id="toDate" onChange={(event) => updateSearch("toDate", event.target.value)} type="date" value={searchParams.get("toDate") || ""} />
          </div>
        </div>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>{tx("Mã đơn", "Order code")}</th>
              <th>{tx("Khách hàng", "Customer")}</th>
              <th>{tx("Ngày đặt", "Order date")}</th>
              <th>{tx("Thanh toán", "Payment")}</th>
              <th>{tx("Tổng tiền", "Total")}</th>
              <th>{tx("Trạng thái", "Status")}</th>
              <th>{tx("Hành động", "Actions")}</th>
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
                <td>{order.paymentMethod?.methodName || tx("Không có", "N/A")}</td>
                <td>{formatCurrency(order.totalAmount)}</td>
                <td>
                  <Badge label={orderStatusLabel(order.orderStatus)} tone={statusTone(order.orderStatus)} />
                </td>
                <td>
                  <Link className="button button-subtle" to={`/admin/orders/${order.orderId}`}>
                    {tx("Chi tiết", "Details")}
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

import { useQuery } from "@tanstack/react-query";
import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Tooltip,
} from "chart.js";
import { Bar, Doughnut, Line } from "react-chartjs-2";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, DashboardData } from "@/lib/api/types";
import { formatCurrency, formatDate, orderStatusLabel, statusTone } from "@/lib/utils/format";

ChartJS.register(ArcElement, BarElement, CategoryScale, Legend, LinearScale, LineElement, PointElement, Tooltip);

export default function DashboardPage() {
  const dashboardQuery = useQuery({
    queryKey: ["admin", "dashboard"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<DashboardData>>("/api/v1/admin/dashboard");
      return response.data;
    },
  });

  if (dashboardQuery.isLoading) {
    return <LoadingScreen label="Đang tải dashboard..." />;
  }

  if (dashboardQuery.isError || !dashboardQuery.data) {
    return <ErrorState message="Không thể tải dữ liệu dashboard quản trị." />;
  }

  const data = dashboardQuery.data;

  return (
    <div className="stack-section">
      <div className="metrics-grid">
        <div className="metric-card">
          <span className="eyebrow">Revenue</span>
          <strong>{formatCurrency(data.revenue)}</strong>
          <span className="muted-copy">Tổng doanh thu theo kỳ mặc định</span>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Orders</span>
          <strong>{data.orderCount}</strong>
          <span className="muted-copy">Tăng trưởng {data.orderGrowth.toFixed(1)}%</span>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Products</span>
          <strong>{data.productCount}</strong>
          <span className="muted-copy">Sản phẩm đang quản lý</span>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Users</span>
          <strong>{data.userCount}</strong>
          <span className="muted-copy">Tài khoản đã đăng ký</span>
        </div>
      </div>

      <div className="split-grid">
        <section className="panel">
          <h2>Doanh thu theo thời gian</h2>
          <Line
            data={{
              labels: data.revenueChart.labels,
              datasets: [
                {
                  label: "Doanh thu",
                  data: data.revenueChart.data,
                  borderColor: "#0c1f3f",
                  backgroundColor: "rgba(201, 161, 76, 0.22)",
                  tension: 0.35,
                  fill: true,
                },
              ],
            }}
            options={{ responsive: true, plugins: { legend: { display: false } } }}
          />
        </section>

        <section className="panel">
          <h2>Đơn hàng theo trạng thái</h2>
          <Doughnut
            data={{
              labels: Object.keys(data.orderStatsByStatus).map(orderStatusLabel),
              datasets: [
                {
                  data: Object.values(data.orderStatsByStatus),
                  backgroundColor: ["#c9a14c", "#356dcb", "#13845f", "#c14a4a", "#5f6674"],
                },
              ],
            }}
          />
        </section>
      </div>

      <div className="split-grid">
        <section className="panel">
          <h2>Phân bổ theo thương hiệu</h2>
          <Bar
            data={{
              labels: data.brandChart.labels,
              datasets: [
                {
                  label: "Đơn hàng",
                  data: data.brandChart.data,
                  backgroundColor: "rgba(12, 31, 63, 0.78)",
                },
              ],
            }}
            options={{ responsive: true }}
          />
        </section>

        <section className="panel">
          <h2>Đơn hàng gần đây</h2>
          <div className="summary-list">
            {data.recentOrders.map((order) => (
              <div className="summary-product" key={order.orderId}>
                <div>
                  <strong>{order.orderCode}</strong>
                  <p className="muted-copy">
                    {order.receiverName} · {formatDate(order.orderDate)}
                  </p>
                </div>
                <div style={{ textAlign: "right" }}>
                  <Badge label={orderStatusLabel(order.orderStatus)} tone={statusTone(order.orderStatus)} />
                  <div style={{ marginTop: 8, fontWeight: 800 }}>{formatCurrency(order.totalAmount)}</div>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}

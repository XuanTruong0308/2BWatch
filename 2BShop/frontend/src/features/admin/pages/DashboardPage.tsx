import { useState } from "react";
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
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 4;

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
  const totalPages = Math.ceil(data.recentOrders.length / itemsPerPage);
  const currentOrders = data.recentOrders.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

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
            {currentOrders.map((order) => (
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
          {totalPages > 1 && (
            <div style={{ display: "flex", gap: "8px", justifyContent: "center", marginTop: "1rem" }}>
              <button 
                type="button" 
                className="button"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
              >
                Trước
              </button>
              <span style={{ padding: "8px" }}>{currentPage} / {totalPages}</span>
              <button 
                type="button" 
                className="button"
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
              >
                Tiếp
              </button>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

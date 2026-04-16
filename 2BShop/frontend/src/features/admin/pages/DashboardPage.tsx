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
import { useI18n } from "@/lib/i18n";
import { formatCurrency, formatDate, orderStatusLabel, statusTone } from "@/lib/utils/format";

ChartJS.register(ArcElement, BarElement, CategoryScale, Legend, LinearScale, LineElement, PointElement, Tooltip);

const chartAxisColor = "#707072";
const chartGridColor = "#e5e5e5";
const chartFont = "Helvetica, Arial, sans-serif";

export default function DashboardPage() {
  const { tx } = useI18n();
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
    return <LoadingScreen label={tx("Đang tải dashboard...", "Loading dashboard...")} />;
  }

  if (dashboardQuery.isError || !dashboardQuery.data) {
    return <ErrorState message={tx("Không thể tải dữ liệu dashboard quản trị.", "We could not load admin dashboard data.")} />;
  }

  const data = dashboardQuery.data;
  const totalPages = Math.ceil(data.recentOrders.length / itemsPerPage);
  const currentOrders = data.recentOrders.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

  return (
    <div className="stack-section">
      <div className="metrics-grid">
        <div className="metric-card">
          <span className="eyebrow">{tx("Doanh thu", "Revenue")}</span>
          <strong>{formatCurrency(data.revenue)}</strong>
          <span className="muted-copy">{tx("Doanh thu trong kỳ báo cáo hiện tại.", "Current reporting window revenue.")}</span>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Đơn hàng", "Orders")}</span>
          <strong>{data.orderCount}</strong>
          <span className="muted-copy">{tx(`Tăng trưởng ${data.orderGrowth.toFixed(1)}%`, `Growth ${data.orderGrowth.toFixed(1)}%`)}</span>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Sản phẩm", "Products")}</span>
          <strong>{data.productCount}</strong>
          <span className="muted-copy">{tx("Số mục sản phẩm đang quản lý.", "Managed catalog entries.")}</span>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Người dùng", "Users")}</span>
          <strong>{data.userCount}</strong>
          <span className="muted-copy">{tx("Tài khoản đã đăng ký.", "Registered accounts.")}</span>
        </div>
      </div>

      <div className="split-grid">
        <section className="panel">
          <div className="section-heading">
            <div>
              <span className="eyebrow">{tx("Hiệu suất", "Performance")}</span>
              <h2>{tx("Doanh thu theo thời gian", "Revenue over time")}</h2>
            </div>
          </div>

          <Line
            data={{
              labels: data.revenueChart.labels,
              datasets: [
                {
                  label: tx("Doanh thu", "Revenue"),
                  data: data.revenueChart.data,
                  borderColor: "#111111",
                  backgroundColor: "rgba(17, 17, 17, 0.08)",
                  pointBackgroundColor: "#111111",
                  pointBorderColor: "#111111",
                  pointRadius: 3,
                  tension: 0.35,
                  fill: true,
                },
              ],
            }}
            options={{
              responsive: true,
              plugins: {
                legend: { display: false },
                tooltip: { titleFont: { family: chartFont }, bodyFont: { family: chartFont } },
              },
              scales: {
                x: { ticks: { color: chartAxisColor }, grid: { color: chartGridColor } },
                y: { ticks: { color: chartAxisColor }, grid: { color: chartGridColor } },
              },
            }}
          />
        </section>

        <section className="panel">
          <div className="section-heading">
            <div>
              <span className="eyebrow">{tx("Vận hành đơn hàng", "Order flow")}</span>
              <h2>{tx("Đơn hàng theo trạng thái", "Orders by status")}</h2>
            </div>
          </div>

          <Doughnut
            data={{
              labels: Object.keys(data.orderStatsByStatus).map(orderStatusLabel),
              datasets: [
                {
                  data: Object.values(data.orderStatsByStatus),
                  backgroundColor: ["#111111", "#4b4b4d", "#007d48", "#d30005", "#cacacb"],
                  borderWidth: 0,
                },
              ],
            }}
            options={{
              plugins: {
                legend: {
                  position: "bottom",
                  labels: { color: chartAxisColor, font: { family: chartFont } },
                },
              },
            }}
          />
        </section>
      </div>

      <div className="split-grid">
        <section className="panel">
          <div className="section-heading">
            <div>
              <span className="eyebrow">{tx("Tỉ lệ thương hiệu", "Brand mix")}</span>
              <h2>{tx("Phân bố thương hiệu", "Brand distribution")}</h2>
            </div>
          </div>

          <Bar
            data={{
              labels: data.brandChart.labels,
              datasets: [
                {
                  label: tx("Đơn hàng", "Orders"),
                  data: data.brandChart.data,
                  backgroundColor: "#111111",
                  borderRadius: 8,
                },
              ],
            }}
            options={{
              responsive: true,
              plugins: {
                legend: { display: false },
                tooltip: { titleFont: { family: chartFont }, bodyFont: { family: chartFont } },
              },
              scales: {
                x: { ticks: { color: chartAxisColor }, grid: { display: false } },
                y: { ticks: { color: chartAxisColor }, grid: { color: chartGridColor } },
              },
            }}
          />
        </section>

        <section className="panel">
          <div className="section-heading">
            <div>
              <span className="eyebrow">{tx("Hoạt động gần đây", "Recent activity")}</span>
              <h2>{tx("Đơn hàng mới nhất", "Latest orders")}</h2>
            </div>
          </div>

          <div className="summary-list">
            {currentOrders.map((order) => (
              <div className="summary-product" key={order.orderId}>
                <div>
                  <strong>{order.orderCode}</strong>
                  <p className="muted-copy">
                    {order.receiverName} - {formatDate(order.orderDate)}
                  </p>
                </div>
                <div style={{ textAlign: "right" }}>
                  <Badge label={orderStatusLabel(order.orderStatus)} tone={statusTone(order.orderStatus)} />
                  <div style={{ marginTop: 8, fontWeight: 700 }}>{formatCurrency(order.totalAmount)}</div>
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 ? (
            <div className="pagination" style={{ justifyContent: "flex-start" }}>
              <button
                type="button"
                className="button button-subtle"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
              >
                {tx("Trước", "Prev")}
              </button>
              <span className="muted-copy" style={{ alignSelf: "center" }}>
                {currentPage} / {totalPages}
              </span>
              <button
                type="button"
                className="button button-subtle"
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
              >
                {tx("Sau", "Next")}
              </button>
            </div>
          ) : null}
        </section>
      </div>
    </div>
  );
}

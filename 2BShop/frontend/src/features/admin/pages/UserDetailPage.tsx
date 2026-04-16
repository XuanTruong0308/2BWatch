import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, User } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { formatCurrency, toBooleanText } from "@/lib/utils/format";

const roleLabel = (role: string, tx: (vi: string, en: string) => string) => {
  switch (role) {
    case "ADMIN":
      return tx("Quản trị vien", "Administrator");
    case "USER":
      return tx("Người dùng", "User");
    default:
      return role;
  }
};

export default function UserDetailPage() {
  const { tx } = useI18n();
  const { id } = useParams();
  const detailQuery = useQuery({
    queryKey: ["admin", "user", id],
    queryFn: async () => {
      const response = await getJson<ApiResponse<User>>(`/api/v1/admin/users/${id}`);
      return response.data;
    },
  });

  if (detailQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải chi tiết người dùng...", "Loading user details...")} />;
  }

  if (detailQuery.isError || !detailQuery.data) {
    return <ErrorState message={tx("Không thể tải chi tiết người dùng.", "Could not load user details.")} />;
  }

  const user = detailQuery.data;

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Chi tiết người dùng", "User detail")}</span>
          <h2>{user.fullName || user.username}</h2>
        </div>
        <Link className="button button-primary" to={`/admin/users/${user.userId}/edit`}>
          {tx("Chinh sua", "Edit")}
        </Link>
      </div>

      <div className="info-grid">
        <div className="metric-card">
          <span className="eyebrow">Username</span>
          <strong>{user.username}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Email</span>
          <strong>{user.email}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Vai tro", "Roles")}</span>
          <strong>{user.roles.map((role) => roleLabel(role, tx)).join(", ")}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Trạng thái", "Status")}</span>
          <strong>{toBooleanText(user.enabled)}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Đơn hàng", "Orders")}</span>
          <strong>{user.orderCount}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Tong chi tieu", "Total spend")}</span>
          <strong>{formatCurrency(user.totalSpent)}</strong>
        </div>
      </div>

      <div className="panel inner-panel" style={{ marginTop: 24 }}>
        <h3>{tx("Thong tin liên hệ", "Contact information")}</h3>
        <p>
          <strong>{tx("Số điện thoại:", "Phone:")}</strong> {user.phone || tx("Chưa cập nhật", "Not updated")}
        </p>
        <p>
          <strong>{tx("Địa chỉ:", "Address:")}</strong> {user.address || tx("Chưa cập nhật", "Not updated")}
        </p>
        <p>
          <strong>{tx("Đăng nhập boi:", "Provider:")}</strong> {user.provider || "LOCAL"}
        </p>
      </div>
    </div>
  );
}

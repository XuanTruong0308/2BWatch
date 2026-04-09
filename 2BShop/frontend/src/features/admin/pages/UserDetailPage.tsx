import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, User } from "@/lib/api/types";
import { formatCurrency, toBooleanText } from "@/lib/utils/format";

export default function UserDetailPage() {
  const { id } = useParams();
  const detailQuery = useQuery({
    queryKey: ["admin", "user", id],
    queryFn: async () => {
      const response = await getJson<ApiResponse<User>>(`/api/v1/admin/users/${id}`);
      return response.data;
    },
  });

  if (detailQuery.isLoading) {
    return <LoadingScreen label="Đang tải chi tiết người dùng..." />;
  }

  if (detailQuery.isError || !detailQuery.data) {
    return <ErrorState message="Không thể tải chi tiết người dùng." />;
  }

  const user = detailQuery.data;

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">User Detail</span>
          <h2>{user.fullName || user.username}</h2>
        </div>
        <Link className="button button-primary" to={`/admin/users/${user.userId}/edit`}>
          Chỉnh sửa
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
          <span className="eyebrow">Vai trò</span>
          <strong>{user.roles.join(", ")}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Trạng thái</span>
          <strong>{toBooleanText(user.enabled)}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Đơn hàng</span>
          <strong>{user.orderCount}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Tổng chi tiêu</span>
          <strong>{formatCurrency(user.totalSpent)}</strong>
        </div>
      </div>

      <div className="panel inner-panel" style={{ marginTop: 24 }}>
        <h3>Thông tin liên hệ</h3>
        <p>
          <strong>Số điện thoại:</strong> {user.phone || "Chưa cập nhật"}
        </p>
        <p>
          <strong>Địa chỉ:</strong> {user.address || "Chưa cập nhật"}
        </p>
        <p>
          <strong>Provider:</strong> {user.provider || "LOCAL"}
        </p>
      </div>
    </div>
  );
}

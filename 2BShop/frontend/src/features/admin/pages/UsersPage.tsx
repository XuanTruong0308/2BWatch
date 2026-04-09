import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { deleteJson, getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, PaginatedResponse, User } from "@/lib/api/types";
import { getErrorMessage, toBooleanText } from "@/lib/utils/format";

export default function UsersPage() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Number(searchParams.get("page") || 0);

  const usersQuery = useQuery({
    queryKey: ["admin", "users", searchParams.toString()],
    queryFn: async () => getJson<PaginatedResponse<User>>(`/api/v1/admin/users?${searchParams.toString()}`),
  });

  const actionMutation = useMutation({
    mutationFn: async ({ endpoint, method }: { endpoint: string; method: "POST" | "DELETE" }) => {
      if (method === "DELETE") {
        return deleteJson(endpoint);
      }
      return postJson(endpoint);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
    },
  });

  if (usersQuery.isLoading) {
    return <LoadingScreen label="Đang tải người dùng..." />;
  }

  if (usersQuery.isError || !usersQuery.data) {
    return <ErrorState message="Không thể tải danh sách người dùng." />;
  }

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
      <div className="panel">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Users</span>
            <h2>Quản lý người dùng</h2>
          </div>
          <Link className="button button-primary" to="/admin/users/new">
            Tạo user
          </Link>
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
              placeholder="Tên, email, username..."
            />
          </div>
          <div className="field-group">
            <label htmlFor="isActive">Trạng thái</label>
            <select className="select" id="isActive" onChange={(event) => updateSearch("isActive", event.target.value)} value={searchParams.get("isActive") || ""}>
              <option value="">Tất cả</option>
              <option value="true">Đang hoạt động</option>
              <option value="false">Đã khóa</option>
            </select>
          </div>
        </div>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Tài khoản</th>
              <th>Liên hệ</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th>Đơn hàng</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {usersQuery.data.items.map((user) => (
              <tr key={user.userId}>
                <td>
                  <strong>{user.fullName || user.username}</strong>
                  <div className="muted-copy">@{user.username}</div>
                </td>
                <td>
                  <div>{user.email}</div>
                  <div className="muted-copy">{user.phone || "Chưa cập nhật"}</div>
                </td>
                <td>{user.roles.join(", ")}</td>
                <td>
                  <Badge label={toBooleanText(user.enabled)} tone={user.enabled ? "success" : "danger"} />
                </td>
                <td>{user.orderCount}</td>
                <td>
                  <div className="header-actions" style={{ justifyContent: "flex-start" }}>
                    <Link className="button button-subtle" to={`/admin/users/${user.userId}`}>
                      Xem
                    </Link>
                    <Link className="button button-subtle" to={`/admin/users/${user.userId}/edit`}>
                      Sửa
                    </Link>
                    {user.enabled ? (
                      <button
                        className="button button-danger"
                        onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/users/${user.userId}/ban`, method: "POST" })}
                        type="button"
                      >
                        Khóa
                      </button>
                    ) : (
                      <button
                        className="button button-subtle"
                        onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/users/${user.userId}/unban`, method: "POST" })}
                        type="button"
                      >
                        Mở khóa
                      </button>
                    )}
                    {!user.roles.includes("ADMIN") ? (
                      <button
                        className="button button-danger"
                        onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/users/${user.userId}`, method: "DELETE" })}
                        type="button"
                      >
                        Xóa
                      </button>
                    ) : null}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {actionMutation.isError ? <p className="inline-text-error">{getErrorMessage(actionMutation.error)}</p> : null}

      <Pagination
        currentPage={page}
        onPageChange={(nextPage) => {
          const next = new URLSearchParams(searchParams);
          next.set("page", String(nextPage));
          setSearchParams(next);
        }}
        totalPages={usersQuery.data.totalPages}
      />
    </div>
  );
}

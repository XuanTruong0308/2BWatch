import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { deleteJson, getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, Brand } from "@/lib/api/types";
import { getErrorMessage, toBooleanText } from "@/lib/utils/format";

export default function BrandsPage() {
  const queryClient = useQueryClient();

  const brandsQuery = useQuery({
    queryKey: ["admin", "brands"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<Brand[]>>("/api/v1/admin/brands");
      return response.data;
    },
  });

  const actionMutation = useMutation({
    mutationFn: async ({ endpoint, method }: { endpoint: string; method: "POST" | "DELETE" }) => {
      if (method === "DELETE") {
        return deleteJson(endpoint);
      }
      return postJson(endpoint);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "brands"] });
    },
  });

  if (brandsQuery.isLoading) {
    return <LoadingScreen label="Đang tải brand..." />;
  }

  if (brandsQuery.isError || !brandsQuery.data) {
    return <ErrorState message="Không thể tải danh sách brand." />;
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Brands</span>
          <h2>Quản lý thương hiệu</h2>
        </div>
        <Link className="button button-primary" to="/admin/brands/new">
          Thêm brand
        </Link>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Tên brand</th>
              <th>Mô tả</th>
              <th>Sản phẩm</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {brandsQuery.data.map((brand) => (
              <tr key={brand.brandId}>
                <td>
                  <strong>{brand.brandName}</strong>
                </td>
                <td>{brand.description || "Chưa có mô tả"}</td>
                <td>{brand.watchCount}</td>
                <td>
                  <Badge label={toBooleanText(brand.active)} tone={brand.active ? "success" : "danger"} />
                </td>
                <td>
                  <div className="header-actions" style={{ justifyContent: "flex-start" }}>
                    <Link className="button button-subtle" to={`/admin/brands/${brand.brandId}/edit`}>
                      Sửa
                    </Link>
                    {!brand.active ? (
                      <button
                        className="button button-subtle"
                        onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/brands/${brand.brandId}/activate`, method: "POST" })}
                        type="button"
                      >
                        Kích hoạt
                      </button>
                    ) : null}
                    <button
                      className="button button-danger"
                      onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/brands/${brand.brandId}`, method: "DELETE" })}
                      type="button"
                    >
                      Xóa
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {actionMutation.isError ? <p className="inline-text-error">{getErrorMessage(actionMutation.error)}</p> : null}
    </div>
  );
}

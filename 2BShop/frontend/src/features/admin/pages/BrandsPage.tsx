import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { deleteJson, getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, Brand } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage, toBooleanText } from "@/lib/utils/format";

export default function BrandsPage() {
  const { tx } = useI18n();
  const queryClient = useQueryClient();
  const [currentPage, setCurrentPage] = useState(0);
  const itemsPerPage = 10;

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
    return <LoadingScreen label={tx("Đang tải thương hiệu...", "Loading brands...")} />;
  }

  if (brandsQuery.isError || !brandsQuery.data) {
    return <ErrorState message={tx("Không thể tải danh sách thương hiệu.", "Could not load brand list.")} />;
  }

  const totalPages = Math.ceil(brandsQuery.data.length / itemsPerPage);
  const currentBrands = brandsQuery.data.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage);

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Thương hiệu", "Brands")}</span>
          <h2>{tx("Quản lý thương hiệu", "Manage brands")}</h2>
        </div>
        <Link className="button button-primary" to="/admin/brands/new">
          {tx("Them thương hiệu", "Add brand")}
        </Link>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>{tx("Tên thương hiệu", "Brand name")}</th>
              <th>{tx("Mô tả", "Description")}</th>
              <th>{tx("Sản phẩm", "Products")}</th>
              <th>{tx("Trạng thái", "Status")}</th>
              <th>{tx("Hành động", "Actions")}</th>
            </tr>
          </thead>
          <tbody>
            {currentBrands.map((brand) => (
              <tr key={brand.brandId}>
                <td>
                  <strong>{brand.brandName}</strong>
                </td>
                <td>{brand.description || tx("Chưa có mô tả", "No description yet")}</td>
                <td>{brand.watchCount}</td>
                <td>
                  <Badge label={toBooleanText(brand.active)} tone={brand.active ? "success" : "danger"} />
                </td>
                <td>
                  <div className="header-actions" style={{ justifyContent: "flex-start" }}>
                    <Link className="button button-subtle" to={`/admin/brands/${brand.brandId}/edit`}>
                      {tx("Sửa", "Edit")}
                    </Link>
                    {!brand.active ? (
                      <button
                        className="button button-subtle"
                        onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/brands/${brand.brandId}/activate`, method: "POST" })}
                        type="button"
                      >
                        {tx("Kích hoạt", "Activate")}
                      </button>
                    ) : null}
                    <button
                      className="button button-danger"
                      onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/brands/${brand.brandId}`, method: "DELETE" })}
                      type="button"
                    >
                      {tx("Xóa", "Delete")}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 ? <Pagination currentPage={currentPage} onPageChange={setCurrentPage} totalPages={totalPages} /> : null}

      {actionMutation.isError ? <p className="inline-text-error">{getErrorMessage(actionMutation.error)}</p> : null}
    </div>
  );
}

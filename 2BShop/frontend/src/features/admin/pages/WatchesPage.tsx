import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { deleteJson, getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, PaginatedResponse, ProductCard, WatchOptionsPayload } from "@/lib/api/types";
import { formatCurrency, getErrorMessage, toBooleanText } from "@/lib/utils/format";

export default function WatchesPage() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Number(searchParams.get("page") || 0);

  const optionsQuery = useQuery({
    queryKey: ["admin", "watch-options"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<WatchOptionsPayload>>("/api/v1/admin/watches/options");
      return response.data;
    },
  });

  const watchesQuery = useQuery({
    queryKey: ["admin", "watches", searchParams.toString()],
    queryFn: async () => getJson<PaginatedResponse<ProductCard>>(`/api/v1/admin/watches?${searchParams.toString()}`),
  });

  const actionMutation = useMutation({
    mutationFn: async ({ endpoint, method }: { endpoint: string; method: "POST" | "DELETE" }) => {
      if (method === "DELETE") {
        return deleteJson(endpoint);
      }
      return postJson(endpoint);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "watches"] });
    },
  });

  if (watchesQuery.isLoading || optionsQuery.isLoading) {
    return <LoadingScreen label="Đang tải sản phẩm..." />;
  }

  if (watchesQuery.isError || !watchesQuery.data || optionsQuery.isError || !optionsQuery.data) {
    return <ErrorState message="Không thể tải danh sách sản phẩm quản trị." />;
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
            <span className="eyebrow">Products</span>
            <h2>Quản lý đồng hồ</h2>
          </div>
          <Link className="button button-primary" to="/admin/watches/new">
            Thêm sản phẩm
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
              placeholder="Tên sản phẩm..."
            />
          </div>
          <div className="field-group">
            <label htmlFor="brandId">Brand</label>
            <select className="select" id="brandId" onChange={(event) => updateSearch("brandId", event.target.value)} value={searchParams.get("brandId") || ""}>
              <option value="">Tất cả</option>
              {optionsQuery.data.brands.map((brand) => (
                <option key={brand.brandId} value={brand.brandId}>
                  {brand.brandName}
                </option>
              ))}
            </select>
          </div>
          <div className="field-group">
            <label htmlFor="categoryId">Danh mục</label>
            <select
              className="select"
              id="categoryId"
              onChange={(event) => updateSearch("categoryId", event.target.value)}
              value={searchParams.get("categoryId") || ""}
            >
              <option value="">Tất cả</option>
              {optionsQuery.data.categories.map((category) => (
                <option key={category.categoryId} value={category.categoryId}>
                  {category.categoryName}
                </option>
              ))}
            </select>
          </div>
          <div className="field-group">
            <label htmlFor="isActive">Trạng thái</label>
            <select className="select" id="isActive" onChange={(event) => updateSearch("isActive", event.target.value)} value={searchParams.get("isActive") || ""}>
              <option value="">Tất cả</option>
              <option value="true">Đang hoạt động</option>
              <option value="false">Đang ẩn</option>
            </select>
          </div>
        </div>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>Brand</th>
              <th>Danh mục</th>
              <th>Giá bán</th>
              <th>Tồn kho</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {watchesQuery.data.items.map((watch) => (
              <tr key={watch.watchId}>
                <td>
                  <strong>{watch.watchName}</strong>
                </td>
                <td>{watch.brandName || "N/A"}</td>
                <td>{watch.categoryName || "N/A"}</td>
                <td>{formatCurrency(watch.priceAfterDiscount)}</td>
                <td>{watch.stockQuantity ?? 0}</td>
                <td>
                  <Badge label={toBooleanText(watch.active)} tone={watch.active ? "success" : "danger"} />
                </td>
                <td>
                  <div className="header-actions" style={{ justifyContent: "flex-start" }}>
                    <Link className="button button-subtle" to={`/admin/watches/${watch.watchId}/edit`}>
                      Sửa
                    </Link>
                    <button
                      className="button button-subtle"
                      onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/watches/${watch.watchId}/toggle-active`, method: "POST" })}
                      type="button"
                    >
                      Đổi trạng thái
                    </button>
                    <button
                      className="button button-danger"
                      onClick={() => actionMutation.mutate({ endpoint: `/api/v1/admin/watches/${watch.watchId}`, method: "DELETE" })}
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

      <Pagination
        currentPage={page}
        onPageChange={(nextPage) => {
          const next = new URLSearchParams(searchParams);
          next.set("page", String(nextPage));
          setSearchParams(next);
        }}
        totalPages={watchesQuery.data.totalPages}
      />
    </div>
  );
}

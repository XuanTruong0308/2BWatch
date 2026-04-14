import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, PaymentMethod } from "@/lib/api/types";
import { getErrorMessage, toBooleanText } from "@/lib/utils/format";

export default function PaymentMethodsPage() {
  const queryClient = useQueryClient();
  const [currentPage, setCurrentPage] = useState(0);
  const itemsPerPage = 10;

  const methodsQuery = useQuery({
    queryKey: ["admin", "payment-methods"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<PaymentMethod[]>>("/api/v1/admin/payments/methods");
      return response.data;
    },
  });

  const toggleMutation = useMutation({
    mutationFn: async (id: number) => postJson(`/api/v1/admin/payments/methods/${id}/toggle-active`),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "payment-methods"] });
    },
  });

  if (methodsQuery.isLoading) {
    return <LoadingScreen label="Đang tải phương thức thanh toán..." />;
  }

  if (methodsQuery.isError || !methodsQuery.data) {
    return <ErrorState message="Không thể tải phương thức thanh toán." />;
  }

  const totalPages = Math.ceil(methodsQuery.data.length / itemsPerPage);
  const currentMethods = methodsQuery.data.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage);

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Payments</span>
          <h2>Phương thức thanh toán</h2>
        </div>
        <Link className="button button-primary" to="/admin/payments/methods/new">
          Thêm phương thức
        </Link>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Tên phương thức</th>
              <th>Mô tả</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {currentMethods.map((method) => (
              <tr key={method.paymentMethodId}>
                <td>
                  <strong>{method.methodName}</strong>
                </td>
                <td>{method.description || "Chưa có mô tả"}</td>
                <td>
                  <Badge label={toBooleanText(method.active)} tone={method.active ? "success" : "danger"} />
                </td>
                <td>
                  <div className="header-actions" style={{ justifyContent: "flex-start" }}>
                    <Link className="button button-subtle" to={`/admin/payments/methods/${method.paymentMethodId}/edit`}>
                      Sửa
                    </Link>
                    <button className="button button-subtle" onClick={() => toggleMutation.mutate(method.paymentMethodId)} type="button">
                      Đổi trạng thái
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <Pagination
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          totalPages={totalPages}
        />
      )}

      {toggleMutation.isError ? <p className="inline-text-error">{getErrorMessage(toggleMutation.error)}</p> : null}
    </div>
  );
}

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, PaymentMethod } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage, toBooleanText } from "@/lib/utils/format";

export default function PaymentMethodsPage() {
  const { tx } = useI18n();
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
    return <LoadingScreen label={tx("Đang tải phương thức thanh toán...", "Loading payment methods...")} />;
  }

  if (methodsQuery.isError || !methodsQuery.data) {
    return <ErrorState message={tx("Không thể tải phương thức thanh toán.", "Could not load payment methods.")} />;
  }

  const totalPages = Math.ceil(methodsQuery.data.length / itemsPerPage);
  const currentMethods = methodsQuery.data.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage);

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Thanh toán", "Payments")}</span>
          <h2>{tx("Phương thức thanh toán", "Payment methods")}</h2>
        </div>
        <Link className="button button-primary" to="/admin/payments/methods/new">
          {tx("Thêm phương thức", "Add method")}
        </Link>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>{tx("Tên phương thức", "Method name")}</th>
              <th>{tx("Mô tả", "Description")}</th>
              <th>{tx("Trạng thái", "Status")}</th>
              <th>{tx("Hành động", "Actions")}</th>
            </tr>
          </thead>
          <tbody>
            {currentMethods.map((method) => (
              <tr key={method.paymentMethodId}>
                <td>
                  <strong>{method.methodName}</strong>
                </td>
                <td>{method.description || tx("Chưa có mô tả", "No description yet")}</td>
                <td>
                  <Badge label={toBooleanText(method.active)} tone={method.active ? "success" : "danger"} />
                </td>
                <td>
                  <div className="header-actions" style={{ justifyContent: "flex-start" }}>
                    <Link className="button button-subtle" to={`/admin/payments/methods/${method.paymentMethodId}/edit`}>
                      {tx("Sửa", "Edit")}
                    </Link>
                    <button className="button button-subtle" onClick={() => toggleMutation.mutate(method.paymentMethodId)} type="button">
                      {tx("Đổi trạng thái", "Toggle status")}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 ? <Pagination currentPage={currentPage} onPageChange={setCurrentPage} totalPages={totalPages} /> : null}

      {toggleMutation.isError ? <p className="inline-text-error">{getErrorMessage(toggleMutation.error)}</p> : null}
    </div>
  );
}

import { useQuery } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, PaymentTransactionsPayload } from "@/lib/api/types";
import { formatCurrency, formatDate, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function TransactionsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Number(searchParams.get("page") || 0);

  const transactionsQuery = useQuery({
    queryKey: ["admin", "transactions", searchParams.toString()],
    queryFn: async () => {
      const response = await getJson<ApiResponse<PaymentTransactionsPayload>>(
        `/api/v1/admin/payments/transactions?${searchParams.toString()}`,
      );
      return response.data;
    },
  });

  if (transactionsQuery.isLoading) {
    return <LoadingScreen label="Đang tải giao dịch..." />;
  }

  if (transactionsQuery.isError || !transactionsQuery.data) {
    return <ErrorState message="Không thể tải giao dịch thanh toán." />;
  }

  const payload = transactionsQuery.data;

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
            <span className="eyebrow">Transactions</span>
            <h2>Giao dịch thanh toán</h2>
          </div>
        </div>

        <div className="form-grid">
          <div className="field-group">
            <label htmlFor="status">Trạng thái</label>
            <select className="select" id="status" onChange={(event) => updateSearch("status", event.target.value)} value={searchParams.get("status") || ""}>
              <option value="">Tất cả</option>
              <option value="PENDING">Pending</option>
              <option value="SUCCESS">Success</option>
              <option value="FAILED">Failed</option>
            </select>
          </div>
          <div className="field-group">
            <label htmlFor="methodId">Phương thức</label>
            <select className="select" id="methodId" onChange={(event) => updateSearch("methodId", event.target.value)} value={searchParams.get("methodId") || ""}>
              <option value="">Tất cả</option>
              {payload.paymentMethods.map((method) => (
                <option key={method.paymentMethodId} value={method.paymentMethodId}>
                  {method.methodName}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Mã giao dịch</th>
              <th>Khách hàng</th>
              <th>Phương thức</th>
              <th>Số tiền</th>
              <th>Thời gian</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {payload.transactions.items.map((transaction) => (
              <tr key={transaction.transactionId}>
                <td>{transaction.transactionCode || `TX-${transaction.transactionId}`}</td>
                <td>{transaction.customerName || "N/A"}</td>
                <td>{transaction.paymentMethod?.methodName || "N/A"}</td>
                <td>{formatCurrency(transaction.amount)}</td>
                <td>{formatDate(transaction.transactionDate)}</td>
                <td>
                  <Badge label={orderStatusLabel(transaction.status)} tone={statusTone(transaction.status)} />
                </td>
                <td>
                  <Link className="button button-subtle" to={`/admin/payments/transactions/${transaction.transactionId}`}>
                    Chi tiết
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Pagination
        currentPage={page}
        onPageChange={(nextPage) => {
          const next = new URLSearchParams(searchParams);
          next.set("page", String(nextPage));
          setSearchParams(next);
        }}
        totalPages={payload.transactions.totalPages}
      />
    </div>
  );
}

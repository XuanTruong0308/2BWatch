import { useQuery } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, PaymentTransactionsPayload } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { formatCurrency, formatDate, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function TransactionsPage() {
  const { tx } = useI18n();
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Number(searchParams.get("page") || 0);

  const transactionsQuery = useQuery({
    queryKey: ["admin", "transactions", searchParams.toString()],
    queryFn: async () => {
      const response = await getJson<ApiResponse<PaymentTransactionsPayload>>(`/api/v1/admin/payments/transactions?${searchParams.toString()}`);
      return response.data;
    },
  });

  if (transactionsQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải giao dịch...", "Loading transactions...")} />;
  }

  if (transactionsQuery.isError || !transactionsQuery.data) {
    return <ErrorState message={tx("Không thể tải giao dịch thanh toán.", "Could not load transactions.")} />;
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
            <span className="eyebrow">{tx("Giao dịch", "Transactions")}</span>
            <h2>{tx("Giao dịch thanh toán", "Payment transactions")}</h2>
          </div>
        </div>

        <div className="form-grid">
          <div className="field-group">
            <label htmlFor="status">{tx("Trạng thái", "Status")}</label>
            <select className="select" id="status" onChange={(event) => updateSearch("status", event.target.value)} value={searchParams.get("status") || ""}>
              <option value="">{tx("Tất cả", "All")}</option>
              <option value="PENDING">{orderStatusLabel("PENDING")}</option>
              <option value="SUCCESS">{orderStatusLabel("SUCCESS")}</option>
              <option value="FAILED">{orderStatusLabel("FAILED")}</option>
            </select>
          </div>
          <div className="field-group">
            <label htmlFor="methodId">{tx("Phương thức", "Method")}</label>
            <select className="select" id="methodId" onChange={(event) => updateSearch("methodId", event.target.value)} value={searchParams.get("methodId") || ""}>
              <option value="">{tx("Tất cả", "All")}</option>
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
              <th>{tx("Mã giao dịch", "Transaction code")}</th>
              <th>{tx("Khách hàng", "Customer")}</th>
              <th>{tx("Phương thức", "Method")}</th>
              <th>{tx("Số tiền", "Amount")}</th>
              <th>{tx("Thời gian", "Time")}</th>
              <th>{tx("Trạng thái", "Status")}</th>
              <th>{tx("Hành động", "Actions")}</th>
            </tr>
          </thead>
          <tbody>
            {payload.transactions.items.map((transaction) => (
              <tr key={transaction.transactionId}>
                <td>{transaction.transactionCode || `TX-${transaction.transactionId}`}</td>
                <td>{transaction.customerName || tx("Không có", "N/A")}</td>
                <td>{transaction.paymentMethod?.methodName || tx("Không có", "N/A")}</td>
                <td>{formatCurrency(transaction.amount)}</td>
                <td>{formatDate(transaction.transactionDate)}</td>
                <td>
                  <Badge label={orderStatusLabel(transaction.status)} tone={statusTone(transaction.status)} />
                </td>
                <td>
                  <Link className="button button-subtle" to={`/admin/payments/transactions/${transaction.transactionId}`}>
                    {tx("Chi tiết", "Details")}
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

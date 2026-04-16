import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, PaymentTransaction } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { formatCurrency, formatDate, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function TransactionDetailPage() {
  const { tx } = useI18n();
  const { id } = useParams();
  const detailQuery = useQuery({
    queryKey: ["admin", "transaction", id],
    queryFn: async () => {
      const response = await getJson<ApiResponse<PaymentTransaction>>(`/api/v1/admin/payments/transactions/${id}`);
      return response.data;
    },
  });

  if (detailQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải chi tiết giao dich...", "Loading transaction details...")} />;
  }

  if (detailQuery.isError || !detailQuery.data) {
    return <ErrorState message={tx("Không thể tải chi tiết giao dich.", "Could not load transaction details.")} />;
  }

  const transaction = detailQuery.data;

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Chi tiết giao dich", "Transaction detail")}</span>
          <h2>{transaction.transactionCode || `TX-${transaction.transactionId}`}</h2>
        </div>
        <Badge label={orderStatusLabel(transaction.status)} tone={statusTone(transaction.status)} />
      </div>

      <div className="info-grid">
        <div className="metric-card">
          <span className="eyebrow">{tx("Số tiền", "Amount")}</span>
          <strong>{formatCurrency(transaction.amount)}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Phương thức", "Method")}</span>
          <strong>{transaction.paymentMethod?.methodName || tx("Không có", "N/A")}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Khách hàng", "Customer")}</span>
          <strong>{transaction.customerName || tx("Không có", "N/A")}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">{tx("Thời gian", "Time")}</span>
          <strong>{formatDate(transaction.transactionDate)}</strong>
        </div>
      </div>

      <div className="panel inner-panel" style={{ marginTop: 24 }}>
        <h3>{tx("Dữ liệu phản hồi", "Response data")}</h3>
        <pre className="response-block">{transaction.responseData || tx("Không có dữ liệu phản hồi.", "No response payload.")}</pre>
      </div>
    </div>
  );
}

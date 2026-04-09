import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, PaymentTransaction } from "@/lib/api/types";
import { formatCurrency, formatDate, orderStatusLabel, statusTone } from "@/lib/utils/format";

export default function TransactionDetailPage() {
  const { id } = useParams();
  const detailQuery = useQuery({
    queryKey: ["admin", "transaction", id],
    queryFn: async () => {
      const response = await getJson<ApiResponse<PaymentTransaction>>(`/api/v1/admin/payments/transactions/${id}`);
      return response.data;
    },
  });

  if (detailQuery.isLoading) {
    return <LoadingScreen label="Đang tải chi tiết giao dịch..." />;
  }

  if (detailQuery.isError || !detailQuery.data) {
    return <ErrorState message="Không thể tải chi tiết giao dịch." />;
  }

  const transaction = detailQuery.data;

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Transaction Detail</span>
          <h2>{transaction.transactionCode || `TX-${transaction.transactionId}`}</h2>
        </div>
        <Badge label={orderStatusLabel(transaction.status)} tone={statusTone(transaction.status)} />
      </div>

      <div className="info-grid">
        <div className="metric-card">
          <span className="eyebrow">Số tiền</span>
          <strong>{formatCurrency(transaction.amount)}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Phương thức</span>
          <strong>{transaction.paymentMethod?.methodName || "N/A"}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Khách hàng</span>
          <strong>{transaction.customerName || "N/A"}</strong>
        </div>
        <div className="metric-card">
          <span className="eyebrow">Thời gian</span>
          <strong>{formatDate(transaction.transactionDate)}</strong>
        </div>
      </div>

      <div className="panel inner-panel" style={{ marginTop: 24 }}>
        <h3>Response data</h3>
        <pre className="response-block">{transaction.responseData || "Không có dữ liệu phản hồi."}</pre>
      </div>
    </div>
  );
}

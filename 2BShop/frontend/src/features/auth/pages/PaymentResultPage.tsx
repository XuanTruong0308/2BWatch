import { Link, useSearchParams } from "react-router-dom";
import { useI18n } from "@/lib/i18n";
import { formatCurrency } from "@/lib/utils/format";

export default function PaymentResultPage() {
  const [searchParams] = useSearchParams();
  const success = searchParams.get("success") === "true";
  const orderId = searchParams.get("orderId");
  const orderCode = searchParams.get("orderCode");
  const amount = Number(searchParams.get("amount") || 0);
  const { tx } = useI18n();
  const message =
    searchParams.get("message") ||
    (success ? tx("Thanh toán thành công.", "Payment completed successfully.") : tx("Thanh toán chưa thành công.", "Payment was not completed."));
  const transactionCode = searchParams.get("transactionCode");
  const bankCode = searchParams.get("bankCode");
  const payDate = searchParams.get("payDate");

  return (
    <section className="auth-shell">
      <div className="panel auth-card">
        <div className={`state-ornament ${success ? "state-ornament-success" : "state-ornament-danger"}`}>
          <i className={`fa-solid ${success ? "fa-check" : "fa-triangle-exclamation"}`} />
        </div>
        <span className="eyebrow">{tx("Ket qua thanh toán", "Payment result")}</span>
        <h1>{success ? tx("Giao dịch đã được ghi nhận.", "The transaction has been recorded.") : tx("Giao dịch chưa hoàn tất.", "The transaction was not completed.")}</h1>
        <p className="muted-copy">{message}</p>

        <div className="info-grid" style={{ marginTop: 20 }}>
          <div className="metric-card">
            <span className="eyebrow">{tx("Đơn hàng", "Order")}</span>
            <strong>{orderCode || orderId || tx("Không có", "N/A")}</strong>
          </div>
          <div className="metric-card">
            <span className="eyebrow">{tx("So tien", "Amount")}</span>
            <strong>{amount ? formatCurrency(amount) : tx("Không có", "N/A")}</strong>
          </div>
          <div className="metric-card">
            <span className="eyebrow">{tx("Ma giao dich", "Transaction code")}</span>
            <strong>{transactionCode || tx("Không có", "N/A")}</strong>
          </div>
          <div className="metric-card">
            <span className="eyebrow">{tx("Ngan hang", "Bank")}</span>
            <strong>{bankCode || tx("Không có", "N/A")}</strong>
          </div>
        </div>

        {payDate ? <p className="muted-copy">{tx("Thoi gian phản hồi", "Response time")}: {payDate}</p> : null}

        <div className="header-actions" style={{ justifyContent: "center", marginTop: 16 }}>
          {orderId ? (
            <>
              <Link className="button button-primary" to={`/checkout/confirmation/${orderId}`}>
                {tx("Xem xác nhận don", "View order confirmation")}
              </Link>
              <Link className="button button-subtle" to={`/my-orders/${orderId}`}>
                {tx("Theo doi đơn hàng", "Track this order")}
              </Link>
            </>
          ) : (
            <Link className="button button-primary" to="/">
              {tx("Quay ve trang chu", "Return home")}
            </Link>
          )}
        </div>
      </div>
    </section>
  );
}

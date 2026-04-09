import { Link, useSearchParams } from "react-router-dom";
import { formatCurrency } from "@/lib/utils/format";

export default function PaymentResultPage() {
  const [searchParams] = useSearchParams();
  const success = searchParams.get("success") === "true";
  const orderId = searchParams.get("orderId");
  const orderCode = searchParams.get("orderCode");
  const amount = Number(searchParams.get("amount") || 0);
  const message = searchParams.get("message") || (success ? "Thanh toán thành công." : "Thanh toán chưa thành công.");
  const transactionCode = searchParams.get("transactionCode");
  const bankCode = searchParams.get("bankCode");
  const payDate = searchParams.get("payDate");

  return (
    <section className="auth-shell">
      <div className="panel auth-card">
        <div className={`state-ornament ${success ? "state-ornament-success" : "state-ornament-danger"}`}>
          <i className={`fa-solid ${success ? "fa-check" : "fa-triangle-exclamation"}`} />
        </div>
        <span className="eyebrow">Payment Result</span>
        <h1>{success ? "Giao dịch đã được ghi nhận." : "Giao dịch chưa hoàn tất."}</h1>
        <p className="muted-copy">{message}</p>

        <div className="info-grid" style={{ marginTop: 20 }}>
          <div className="metric-card">
            <span className="eyebrow">Đơn hàng</span>
            <strong>{orderCode || orderId || "N/A"}</strong>
          </div>
          <div className="metric-card">
            <span className="eyebrow">Số tiền</span>
            <strong>{amount ? formatCurrency(amount) : "N/A"}</strong>
          </div>
          <div className="metric-card">
            <span className="eyebrow">Mã giao dịch</span>
            <strong>{transactionCode || "N/A"}</strong>
          </div>
          <div className="metric-card">
            <span className="eyebrow">Ngân hàng</span>
            <strong>{bankCode || "N/A"}</strong>
          </div>
        </div>

        {payDate ? <p className="muted-copy">Thời gian phản hồi: {payDate}</p> : null}

        <div className="header-actions" style={{ justifyContent: "center", marginTop: 16 }}>
          {orderId ? (
            <>
              <Link className="button button-primary" to={`/checkout/confirmation/${orderId}`}>
                Xem xác nhận đơn
              </Link>
              <Link className="button button-subtle" to={`/my-orders/${orderId}`}>
                Theo dõi đơn hàng
              </Link>
            </>
          ) : (
            <Link className="button button-primary" to="/">
              Quay về trang chủ
            </Link>
          )}
        </div>
      </div>
    </section>
  );
}

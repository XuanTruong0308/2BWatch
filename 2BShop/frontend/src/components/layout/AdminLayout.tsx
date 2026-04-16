import { Link, NavLink, Outlet } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { useCsrf } from "@/hooks/useCsrf";
import { useI18n } from "@/lib/i18n";

const adminLinks = [
  { to: "/admin/dashboard", labelVi: "Bảng điều khiển", labelEn: "Dashboard" },
  { to: "/admin/watches", labelVi: "Sản phẩm", labelEn: "Products" },
  { to: "/admin/brands", labelVi: "Thương hiệu", labelEn: "Brands" },
  { to: "/admin/orders", labelVi: "Đơn hàng", labelEn: "Orders" },
  { to: "/admin/users", labelVi: "Người dùng", labelEn: "Users" },
  { to: "/admin/payments/methods", labelVi: "Thanh toán", labelEn: "Payments" },
  { to: "/admin/payments/transactions", labelVi: "Giao dịch", labelEn: "Transactions" },
  { to: "/admin/bank-accounts", labelVi: "Ngân hàng", labelEn: "Banking" },
  { to: "/admin/support-chat", labelVi: "Hỗ trợ chat", labelEn: "Support chat" },
];

export function AdminLayout() {
  const { data: user } = useAuth();
  const { data: csrf } = useCsrf();
  const { language, setLanguage, tx } = useI18n();

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div>
          <span className="eyebrow">{tx("Backoffice", "Backoffice")}</span>
          <h2>2BShop Console</h2>
          <p className="muted-copy">{tx("Giao diện quản trị gọn, rõ và đổi song ngữ toàn bộ.", "A clean admin surface with full bilingual switching.")}</p>
        </div>

        <nav className="admin-nav">
          {adminLinks.map((link) => (
            <NavLink key={link.to} to={link.to} className={({ isActive }) => (isActive ? "active-link" : "")}>
              {language === "vi" ? link.labelVi : link.labelEn}
            </NavLink>
          ))}
        </nav>

        <div className="admin-sidebar__footer">
          <span>{user?.fullName ?? user?.email}</span>
          <Link to="/">{tx("Xem storefront", "View storefront")}</Link>
        </div>
      </aside>

      <div className="admin-main">
        <header className="admin-topbar">
          <div>
            <span className="eyebrow">{tx("Chế độ song ngữ", "Bilingual mode")}</span>
            <h1>{tx("Vận hành hệ thống 2BShop", "Run the 2BShop system")}</h1>
          </div>

          <div className="header-actions">
            <button
              className="button button-subtle language-toggle"
              onClick={() => setLanguage(language === "vi" ? "en" : "vi")}
              type="button"
            >
              {language === "vi" ? "ENG" : "VI"}
            </button>
            <form action="/logout" method="post">
              {csrf ? <input name={csrf.parameterName} type="hidden" value={csrf.token} /> : null}
              <button className="button button-subtle" type="submit">
                {tx("Đăng xuất", "Sign out")}
              </button>
            </form>
          </div>
        </header>

        <main className="admin-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

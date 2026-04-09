import { Link, Outlet } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { useCsrf } from "@/hooks/useCsrf";

const adminLinks = [
  { to: "/admin/dashboard", label: "Dashboard" },
  { to: "/admin/watches", label: "Sản phẩm" },
  { to: "/admin/brands", label: "Brand" },
  { to: "/admin/orders", label: "Đơn hàng" },
  { to: "/admin/users", label: "Người dùng" },
  { to: "/admin/payments/methods", label: "Thanh toán" },
  { to: "/admin/payments/transactions", label: "Giao dịch" },
  { to: "/admin/bank-accounts", label: "Ngân hàng" },
];

export function AdminLayout() {
  const { data: user } = useAuth();
  const { data: csrf } = useCsrf();

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div>
          <span className="eyebrow">Admin</span>
          <h2>2BShop Console</h2>
        </div>
        <nav className="admin-nav">
          {adminLinks.map((link) => (
            <Link key={link.to} to={link.to}>
              {link.label}
            </Link>
          ))}
        </nav>
        <div className="admin-sidebar__footer">
          <span>{user?.fullName ?? user?.email}</span>
          <Link to="/">Về storefront</Link>
        </div>
      </aside>

      <div className="admin-main">
        <header className="admin-topbar">
          <div>
            <span className="eyebrow">Backoffice</span>
            <h1>Điều hành 2BShop</h1>
          </div>
          <form action="/logout" method="post">
            {csrf ? <input name={csrf.parameterName} type="hidden" value={csrf.token} /> : null}
            <button className="button button-subtle" type="submit">
              Đăng xuất
            </button>
          </form>
        </header>

        <main className="admin-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

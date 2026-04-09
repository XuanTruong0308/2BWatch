import { useQuery } from "@tanstack/react-query";
import { Link, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { useCsrf } from "@/hooks/useCsrf";
import { getJson } from "@/lib/api/client";
import type { ApiResponse } from "@/lib/api/types";

export function PublicLayout() {
  const location = useLocation();
  const { data: user } = useAuth();
  const { data: csrf } = useCsrf();
  const { data: cartCount } = useQuery({
    queryKey: ["cart", "count"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<{ count: number }>>("/api/v1/cart/count");
      return response.data.count;
    },
  });

  return (
    <div className="app-shell">
      <header className="site-header">
        <div className="site-header__top">
          <Link className="brand-mark" to="/">
            2BShop
          </Link>
          <form action="/watches" className="site-search">
            <input name="search" placeholder="Tìm kiếm đồng hồ, thương hiệu, bộ sưu tập..." />
            <button type="submit">
              <i className="fa-solid fa-magnifying-glass" />
            </button>
          </form>
          <div className="utility-copy">
            <span>Hotline</span>
            <strong>0399 760 075</strong>
          </div>
        </div>

        <div className="site-header__nav">
          <nav className="main-nav">
            <Link to="/">Trang chủ</Link>
            <Link to="/watches">Sản phẩm</Link>
            <Link to="/watches/discount">Giảm giá</Link>
            <Link to="/watches/newest">Mới nhất</Link>
            <Link to="/about">Giới thiệu</Link>
          </nav>

          <div className="header-actions">
            {user?.authenticated ? (
              <>
                {user.admin ? (
                  <Link to="/admin/dashboard">Quản trị</Link>
                ) : (
                  <>
                    <Link to="/profile">Xin chào, {user.fullName ?? user.email}</Link>
                    <Link to="/my-orders">Đơn hàng</Link>
                  </>
                )}
                <form action="/logout" method="post">
                  {csrf ? <input name={csrf.parameterName} type="hidden" value={csrf.token} /> : null}
                  <button className="button button-subtle" type="submit">
                    Đăng xuất
                  </button>
                </form>
              </>
            ) : (
              <Link to="/login">Đăng nhập</Link>
            )}
            <Link className="cart-pill" to="/cart">
              <i className="fa-solid fa-bag-shopping" />
              <span>{cartCount ?? 0}</span>
            </Link>
          </div>
        </div>
      </header>

      <main className={location.pathname === "/" ? "home-shell" : "content-shell"}>
        <Outlet />
      </main>

      <footer className="site-footer">
        <div>
          <span className="eyebrow">2BShop</span>
          <h3>Đồng hồ mang thần thái hiện đại, sang trọng và chuẩn thương hiệu.</h3>
        </div>
        <div className="footer-grid">
          <div>
            <h4>Khám phá</h4>
            <Link to="/watches">Bộ sưu tập</Link>
            <Link to="/watches/newest">Hàng mới về</Link>
            <Link to="/watches/discount">Ưu đãi</Link>
          </div>
          <div>
            <h4>Hỗ trợ</h4>
            <Link to="/policy">Chính sách</Link>
            <Link to="/terms">Điều khoản</Link>
            <Link to="/faq">Câu hỏi thường gặp</Link>
            <Link to="/my-orders">Theo dõi đơn hàng</Link>
          </div>
          <div>
            <h4>Kết nối</h4>
            <a href="tel:0399760075">0399 760 075</a>
            <a href="mailto:boiznews.fpoly@gmail.com">boiznews.fpoly@gmail.com</a>
            <span>Ho Chi Minh City, Vietnam</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

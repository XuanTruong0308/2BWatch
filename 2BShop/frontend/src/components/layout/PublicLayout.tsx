import { useQuery } from "@tanstack/react-query";
import { Link, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { useCsrf } from "@/hooks/useCsrf";
import { getJson } from "@/lib/api/client";
import { useI18n } from "@/lib/i18n";
import type { ApiResponse } from "@/lib/api/types";
import { ChatBubble } from "@/components/ui/ChatBubble";

export function PublicLayout() {
  const location = useLocation();
  const { data: user } = useAuth();
  const { data: csrf } = useCsrf();
  const { language, setLanguage, tx } = useI18n();
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
        <div className="site-header__promo-bar">
          <div className="site-header__promo">
            <span>{tx("Mua sắm với thương hiệu uy tín cao, thao tác nhanh hơn.", "Shop with a cleaner new interface.")}</span>
            <span>{tx("Miễn phí giao hàng từ 500.000đ.", "Free shipping from 500,000 VND.")}</span>
          </div>
        </div>

        <div className="site-header__top">
          <Link className="brand-mark" to="/">
            2BShop
          </Link>

          <form action="/watches" className="site-search">
            <input name="search" placeholder={tx("Tìm kiếm đồng hồ, thương hiệu, bộ sưu tập...", "Search watches, brands, collections...")} />
            <button type="submit" aria-label={tx("Tìm kiếm", "Search")}>
              <i className="fa-solid fa-magnifying-glass" />
            </button>
          </form>

          <div className="utility-copy">
            <span>{tx("Hotline", "Hotline")}</span>
            <strong>0399 760 075</strong>
          </div>
        </div>

        <div className="site-header__nav">
          <nav className="main-nav">
            <Link to="/">{tx("Trang chủ", "Home")}</Link>
            <Link to="/watches">{tx("Sản phẩm", "Products")}</Link>
            <Link to="/watches/discount">{tx("Giảm giá", "Discount")}</Link>
            <Link to="/watches/newest">{tx("Mới nhất", "Newest")}</Link>
            <Link to="/about">{tx("Giới thiệu", "About")}</Link>
          </nav>

          <div className="header-actions">
            <button
              className="button button-subtle language-toggle"
              onClick={() => setLanguage(language === "vi" ? "en" : "vi")}
              type="button"
            >
              {language === "vi" ? "ENG" : "VI"}
            </button>

            {user?.authenticated ? (
              <>
                {user.admin ? (
                  <Link to="/admin/dashboard">{tx("Quản trị", "Admin")}</Link>
                ) : (
                  <>
                    <Link to="/profile">{user.fullName ?? user.email}</Link>
                    <Link to="/my-orders">{tx("Đơn hàng", "Orders")}</Link>
                  </>
                )}
                <form action="/logout" method="post">
                  {csrf ? <input name={csrf.parameterName} type="hidden" value={csrf.token} /> : null}
                  <button className="button button-subtle" type="submit">
                    {tx("Đăng xuất", "Sign out")}
                  </button>
                </form>
              </>
            ) : (
              <Link to="/login">{tx("Đăng nhập", "Sign in")}</Link>
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
        <div className="site-footer__inner">

          <div className="footer-grid">
            <div>
              <h4>{tx("Mua sắm", "Shop")}</h4>
              <Link to="/watches">{tx("Bộ sưu tập", "Collection")}</Link>
              <Link to="/watches/newest">{tx("Hàng mới", "Newest drops")}</Link>
              <Link to="/watches/discount">{tx("Ưu đãi", "Sale edit")}</Link>
            </div>

            <div>
              <h4>{tx("Hỗ trợ", "Support")}</h4>
              <Link to="/policy">{tx("Chính sách", "Policy")}</Link>
              <Link to="/terms">{tx("Điều khoản", "Terms")}</Link>
              <Link to="/faq">FAQ</Link>
              <Link to="/my-orders">{tx("Theo dõi đơn", "Track order")}</Link>
            </div>

            <div>
              <h4>{tx("Doanh nghiệp", "Company")}</h4>
              <Link to="/about">{tx("Về 2BShop", "About 2BShop")}</Link>
              <span>Đà Nẵng, Việt Nam</span>
              <a href="mailto:boiznews.fpoly@gmail.com">boiznews.fpoly@gmail.com</a>
            </div>

            <div>
              <h4>{tx("Kết nối", "Connect")}</h4>
              <a href="tel:0399760075">0399 760 075</a>
              <span>{tx("Phản hồi trong vài phút", "Replies in a few minutes")}</span>
              <span>{tx("Chat hỗ trợ theo session", "Session-based support chat")}</span>
            </div>
          </div>
        </div>
      </footer>

      <ChatBubble />
    </div>
  );
}

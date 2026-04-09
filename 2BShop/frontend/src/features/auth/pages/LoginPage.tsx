import { Navigate, useSearchParams } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { useCsrf } from "@/hooks/useCsrf";

const messageMap: Record<string, string> = {
  error: "Email hoặc mật khẩu chưa chính xác. Vui lòng thử lại.",
  logout: "Phiên đăng nhập đã được kết thúc an toàn.",
  oauth2: "Đăng nhập mạng xã hội chưa thành công. Vui lòng thử lại.",
};

export default function LoginPage() {
  const [searchParams] = useSearchParams();
  const { data: user, isLoading } = useAuth();
  const { data: csrf } = useCsrf();

  const continueTo = searchParams.get("continue");
  const redirectTo = continueTo || (user?.admin ? "/admin/dashboard" : "/");

  if (!isLoading && user?.authenticated) {
    return <Navigate replace to={redirectTo} />;
  }

  const messageKey = ["error", "logout", "oauth2"].find((key) => searchParams.has(key));
  const hint = messageKey ? messageMap[messageKey] : null;

  return (
    <section className="auth-shell">
      <div className="panel auth-card">
        <span className="eyebrow">Welcome Back</span>
        <h1>Đăng nhập vào trải nghiệm mua sắm mới của 2BShop.</h1>
        <p className="muted-copy">
          Giữ nguyên xác thực Spring Security hiện tại, nhưng giao diện đã được làm sáng, gọn và mượt hơn cho cả khách
          hàng lẫn quản trị viên.
        </p>

        {hint ? <div className="inline-alert inline-alert-warning">{hint}</div> : null}

        <form action="/perform-login" className="form-grid" method="post">
          {csrf ? <input name={csrf.parameterName} type="hidden" value={csrf.token} /> : null}
          {continueTo ? <input name="continue" type="hidden" value={continueTo} /> : null}

          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="email">Email</label>
            <input className="field" id="email" name="email" placeholder="you@example.com" required type="email" />
          </div>

          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="password">Mật khẩu</label>
            <input className="field" id="password" name="password" placeholder="Nhập mật khẩu" required type="password" />
          </div>

          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <a className="muted-copy" href="/forgot-password">
              Quên mật khẩu?
            </a>
            <button className="button button-primary" type="submit">
              Đăng nhập
            </button>
          </div>
        </form>

        <div className="auth-divider">hoặc tiếp tục bằng</div>

        <div className="header-actions" style={{ justifyContent: "stretch" }}>
          <a className="button button-subtle social-button" href="/oauth2/authorization/google">
            <i className="fa-brands fa-google" />
            <span>Đăng nhập với Google</span>
          </a>
        </div>

        <p className="muted-copy" style={{ marginBottom: 0 }}>
          Chưa có tài khoản? <a href="/register">Tạo tài khoản mới</a>
        </p>
      </div>
    </section>
  );
}

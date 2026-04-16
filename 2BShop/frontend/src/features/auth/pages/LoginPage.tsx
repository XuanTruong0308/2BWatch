import { Navigate, useSearchParams } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { useCsrf } from "@/hooks/useCsrf";
import { useI18n } from "@/lib/i18n";

export default function LoginPage() {
  const [searchParams] = useSearchParams();
  const { data: user, isLoading } = useAuth();
  const { data: csrf } = useCsrf();
  const { tx } = useI18n();

  const messageMap = {
    error: tx("Email hoặc mật khẩu chưa đúng. Vui lòng thử lại.", "Your email or password was incorrect. Please try again."),
    logout: tx("Bạn đã đăng xuất an toàn.", "You have been signed out safely."),
    oauth2: tx("Đăng nhập mạng xã hội chưa hoàn tất. Vui lòng thử lại.", "Social login was not completed. Please try again."),
  };

  const continueTo = searchParams.get("continue");
  const redirectTo = continueTo || (user?.admin ? "/admin/dashboard" : "/");

  if (!isLoading && user?.authenticated) {
    return <Navigate replace to={redirectTo} />;
  }

  const messageKey = ["error", "logout", "oauth2"].find((key) => searchParams.has(key));
  const hint = messageKey ? messageMap[messageKey as keyof typeof messageMap] : null;

  return (
    <section className="auth-shell">
      <div className="panel auth-card">
        <span className="eyebrow">{tx("Chào mừng quay lại", "Welcome back")}</span>
        <h1>{tx("Đăng nhập để tiếp tục trải nghiệm 2BShop.", "Sign in to continue your 2BShop experience.")}</h1>
        <p className="muted-copy"></p>

        {hint ? <div className="inline-alert inline-alert-warning">{hint}</div> : null}

        <form action="/perform-login" className="form-grid" method="post">
          {csrf ? <input name={csrf.parameterName} type="hidden" value={csrf.token} /> : null}
          {continueTo ? <input name="continue" type="hidden" value={continueTo} /> : null}

          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="email">{tx("Email", "Email")}</label>
            <input className="field" id="email" name="email" placeholder="you@example.com" required type="email" />
          </div>

          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="password">{tx("Mật khẩu", "Password")}</label>
            <input className="field" id="password" name="password" placeholder={tx("Nhập mật khẩu", "Enter password")} required type="password" />
          </div>

          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <a className="muted-copy" href="/forgot-password">
              {tx("Quen mật khẩu?", "Forgot password?")}
            </a>
            <button className="button button-primary" type="submit">
              {tx("Đăng nhập", "Sign in")}
            </button>
          </div>
        </form>

        <div className="auth-divider">{tx("hoặc tiếp tục bằng", "or continue with")}</div>

        <div className="header-actions" style={{ justifyContent: "stretch" }}>
          <a className="button button-subtle social-button" href="/oauth2/authorization/google">
            <i className="fa-brands fa-google" />
            <span>{tx("Đăng nhập với Google", "Sign in with Google")}</span>
          </a>
        </div>

        <p className="muted-copy" style={{ marginBottom: 0 }}>
          {tx("Chưa có tài khoản?", "Need an account?")} <a href="/register">{tx("Tạo tài khoản", "Create one now")}</a>
        </p>
      </div>
    </section>
  );
}

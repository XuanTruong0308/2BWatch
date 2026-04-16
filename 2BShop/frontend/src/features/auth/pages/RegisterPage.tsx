import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { postJson } from "@/lib/api/client";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage } from "@/lib/utils/format";

type RegisterValues = {
  fullName: string;
  userName: string;
  email: string;
  phone: string;
  password: string;
  confirmPassword: string;
};

type EmailValues = {
  email: string;
};

export default function RegisterPage() {
  const form = useForm<RegisterValues>();
  const emailForm = useForm<EmailValues>();
  const { tx } = useI18n();

  const registerMutation = useMutation({
    mutationFn: async (values: RegisterValues) => postJson("/api/v1/auth/register", values),
    onSuccess: (_, values) => {
      emailForm.reset({ email: values.email });
    },
  });

  const resendMutation = useMutation({
    mutationFn: async (values: EmailValues) => postJson("/api/v1/auth/resend-verification", values),
  });

  return (
    <section className="auth-shell">
      <div className="panel auth-card auth-card-wide">
        <span className="eyebrow">{tx("Tạo tài khoản", "Create account")}</span>
        <h1>{tx("Mở tài khoản 2BShop chỉ trong vài bước.", "Open your 2BShop account in just a few steps.")}</h1>
        <p className="muted-copy">
          {tx(
            "Đăng ký, xác thực email và toàn bộ giao diện này đều có thể đổi giữa tiếng Việt và tiếng Anh.",
            "Registration, email verification, and this full interface can switch between Vietnamese and English.",
          )}
        </p>

        {registerMutation.isSuccess ? (
          <div className="inline-alert inline-alert-success">
            {tx("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản trước khi đăng nhập.", "Registration succeeded. Please check your inbox and verify the account before signing in.")}
          </div>
        ) : null}
        {registerMutation.isError ? (
          <div className="inline-alert inline-alert-danger">{getErrorMessage(registerMutation.error)}</div>
        ) : null}

        <form className="form-grid" onSubmit={form.handleSubmit((values) => registerMutation.mutate(values))}>
          <div className="field-group">
            <label htmlFor="fullName">{tx("Họ và tên", "Full name")}</label>
            <input className="field" id="fullName" {...form.register("fullName", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="userName">{tx("Tên đăng nhập", "Username")}</label>
            <input className="field" id="userName" {...form.register("userName", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="email">Email</label>
            <input className="field" id="email" type="email" {...form.register("email", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="phone">{tx("Số điện thoại", "Phone")}</label>
            <input className="field" id="phone" {...form.register("phone", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="password">{tx("Mật khẩu", "Password")}</label>
            <input className="field" id="password" type="password" {...form.register("password", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="confirmPassword">{tx("Xác nhận mật khẩu", "Confirm password")}</label>
            <input
              className="field"
              id="confirmPassword"
              type="password"
              {...form.register("confirmPassword", { required: true })}
            />
          </div>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label className="checkbox-row">
              <input required type="checkbox" />
              <span>{tx("Tôi đồng ý với điều khoản và chính sách mua sắm của 2BShop.", "I agree with the store terms and purchase policy of 2BShop.")}</span>
            </label>
          </div>
          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <a className="muted-copy" href="/login">
              {tx("Đã có tài khoản? Đăng nhập", "Already have an account? Sign in")}
            </a>
            <button className="button button-primary" disabled={registerMutation.isPending} type="submit">
              {registerMutation.isPending ? tx("Đang tạo tài khoản...", "Creating account...") : tx("Tạo tài khoản", "Create account")}
            </button>
          </div>
        </form>

        <div className="panel inner-panel">
          <h3>{tx("Chưa nhận được email xác thực?", "No verification email yet?")}</h3>
          <form
            className="header-actions"
            onSubmit={emailForm.handleSubmit((values) => resendMutation.mutate(values))}
            style={{ alignItems: "end", flexWrap: "wrap", justifyContent: "flex-start" }}
          >
            <div className="field-group" style={{ minWidth: 260 }}>
              <label htmlFor="verifyEmail">{tx("Email cần gửi lại", "Email to resend")}</label>
              <input className="field" id="verifyEmail" type="email" {...emailForm.register("email", { required: true })} />
            </div>
            <button className="button button-subtle" disabled={resendMutation.isPending} type="submit">
              {tx("Gửi lại xác thực", "Resend verification")}
            </button>
          </form>
          {resendMutation.isSuccess ? (
            <p className="muted-copy" style={{ marginBottom: 0 }}>
              {tx("Email xác thực đã được gửi lại.", "Verification email sent again.")}
            </p>
          ) : null}
          {resendMutation.isError ? <p className="inline-text-error">{getErrorMessage(resendMutation.error)}</p> : null}
        </div>
      </div>
    </section>
  );
}

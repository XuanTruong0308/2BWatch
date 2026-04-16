import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { postJson } from "@/lib/api/client";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage } from "@/lib/utils/format";

type ForgotValues = {
  email: string;
};

export default function ForgotPasswordPage() {
  const form = useForm<ForgotValues>();
  const mutation = useMutation({
    mutationFn: async (values: ForgotValues) => postJson("/api/v1/auth/forgot-password", values),
  });
  const { tx } = useI18n();

  return (
    <section className="auth-shell">
      <div className="panel auth-card">
        <span className="eyebrow">{tx("Khôi phục mật khẩu", "Password recovery")}</span>
        <h1>{tx("Đặt lại mật khẩu qua email.", "Reset your password through email.")}</h1>
        <p className="muted-copy">
          {tx(
            "Flow reset mật khẩu vẫn dùng backend hiện tại, nhưng giao diện có thể đổi song ngữ ngay lập tức.",
            "The reset-password flow still uses the current backend rules, but the interface can switch languages instantly.",
          )}
        </p>

        {mutation.isSuccess ? (
          <div className="inline-alert inline-alert-success">
            {tx("Nếu email tồn tại trong hệ thống, liên kết đặt lại mật khẩu đã được gửi.", "If the email exists in our system, a reset link has been sent.")}
          </div>
        ) : null}
        {mutation.isError ? <div className="inline-alert inline-alert-danger">{getErrorMessage(mutation.error)}</div> : null}

        <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="email">{tx("Email tài khoản", "Account email")}</label>
            <input className="field" id="email" type="email" {...form.register("email", { required: true })} />
          </div>
          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <a className="muted-copy" href="/login">
              {tx("Quay lại đăng nhập", "Back to sign in")}
            </a>
            <button className="button button-primary" disabled={mutation.isPending} type="submit">
              {mutation.isPending ? tx("Đang gửi...", "Sending...") : tx("Gửi liên kết đặt lại", "Send reset link")}
            </button>
          </div>
        </form>
      </div>
    </section>
  );
}

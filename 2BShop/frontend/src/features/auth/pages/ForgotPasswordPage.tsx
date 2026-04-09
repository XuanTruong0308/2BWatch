import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { postJson } from "@/lib/api/client";
import { getErrorMessage } from "@/lib/utils/format";

type ForgotValues = {
  email: string;
};

export default function ForgotPasswordPage() {
  const form = useForm<ForgotValues>();
  const mutation = useMutation({
    mutationFn: async (values: ForgotValues) => postJson("/api/v1/auth/forgot-password", values),
  });

  return (
    <section className="auth-shell">
      <div className="panel auth-card">
        <span className="eyebrow">Password Recovery</span>
        <h1>Đặt lại mật khẩu an toàn qua email.</h1>
        <p className="muted-copy">
          Chúng tôi vẫn dùng flow reset password hiện tại của backend, nhưng đã rút gọn phần hiển thị để người dùng mới
          không bị rối.
        </p>

        {mutation.isSuccess ? (
          <div className="inline-alert inline-alert-success">
            Nếu email tồn tại trong hệ thống, liên kết đặt lại mật khẩu đã được gửi.
          </div>
        ) : null}
        {mutation.isError ? <div className="inline-alert inline-alert-danger">{getErrorMessage(mutation.error)}</div> : null}

        <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="email">Email tài khoản</label>
            <input className="field" id="email" type="email" {...form.register("email", { required: true })} />
          </div>
          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <a className="muted-copy" href="/login">
              Quay lại đăng nhập
            </a>
            <button className="button button-primary" disabled={mutation.isPending} type="submit">
              {mutation.isPending ? "Đang gửi..." : "Gửi liên kết đặt lại"}
            </button>
          </div>
        </form>
      </div>
    </section>
  );
}

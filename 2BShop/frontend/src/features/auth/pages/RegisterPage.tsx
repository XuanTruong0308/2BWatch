import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { postJson } from "@/lib/api/client";
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
        <span className="eyebrow">Create Account</span>
        <h1>Mở tài khoản 2BShop chỉ trong vài bước.</h1>
        <p className="muted-copy">
          Luồng đăng ký vẫn dùng logic xác thực email cũ, còn phần hiển thị được sắp xếp lại để rõ ràng hơn trên cả
          desktop và mobile.
        </p>

        {registerMutation.isSuccess ? (
          <div className="inline-alert inline-alert-success">
            Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản trước khi đăng nhập.
          </div>
        ) : null}
        {registerMutation.isError ? (
          <div className="inline-alert inline-alert-danger">{getErrorMessage(registerMutation.error)}</div>
        ) : null}

        <form className="form-grid" onSubmit={form.handleSubmit((values) => registerMutation.mutate(values))}>
          <div className="field-group">
            <label htmlFor="fullName">Họ và tên</label>
            <input className="field" id="fullName" {...form.register("fullName", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="userName">Tên đăng nhập</label>
            <input className="field" id="userName" {...form.register("userName", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="email">Email</label>
            <input className="field" id="email" type="email" {...form.register("email", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="phone">Số điện thoại</label>
            <input className="field" id="phone" {...form.register("phone", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="password">Mật khẩu</label>
            <input className="field" id="password" type="password" {...form.register("password", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="confirmPassword">Xác nhận mật khẩu</label>
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
              <span>Tôi đồng ý với điều khoản sử dụng và chính sách mua sắm của 2BShop.</span>
            </label>
          </div>
          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <a className="muted-copy" href="/login">
              Đã có tài khoản? Đăng nhập
            </a>
            <button className="button button-primary" disabled={registerMutation.isPending} type="submit">
              {registerMutation.isPending ? "Đang tạo tài khoản..." : "Đăng ký"}
            </button>
          </div>
        </form>

        <div className="panel inner-panel">
          <h3>Chưa nhận được email xác thực?</h3>
          <form
            className="header-actions"
            onSubmit={emailForm.handleSubmit((values) => resendMutation.mutate(values))}
            style={{ alignItems: "end", flexWrap: "wrap", justifyContent: "flex-start" }}
          >
            <div className="field-group" style={{ minWidth: 260 }}>
              <label htmlFor="verifyEmail">Email cần gửi lại</label>
              <input className="field" id="verifyEmail" type="email" {...emailForm.register("email", { required: true })} />
            </div>
            <button className="button button-subtle" disabled={resendMutation.isPending} type="submit">
              Gửi lại email xác thực
            </button>
          </form>
          {resendMutation.isSuccess ? (
            <p className="muted-copy" style={{ marginBottom: 0 }}>
              Email xác thực đã được gửi lại.
            </p>
          ) : null}
          {resendMutation.isError ? <p className="inline-text-error">{getErrorMessage(resendMutation.error)}</p> : null}
        </div>
      </div>
    </section>
  );
}

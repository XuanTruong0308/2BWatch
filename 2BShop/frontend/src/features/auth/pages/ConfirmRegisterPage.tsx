import { useEffect, useRef } from "react";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useSearchParams } from "react-router-dom";
import { postJson } from "@/lib/api/client";
import { getErrorMessage } from "@/lib/utils/format";

type ConfirmValues = {
  email: string;
  token: string;
};

export default function ConfirmRegisterPage() {
  const [searchParams] = useSearchParams();
  const form = useForm<ConfirmValues>({
    defaultValues: {
      email: searchParams.get("email") || "",
      token: searchParams.get("token") || "",
    },
  });
  const autoSubmitted = useRef(false);

  const confirmMutation = useMutation({
    mutationFn: async (values: ConfirmValues) => postJson("/api/v1/auth/confirm-register", values),
  });

  useEffect(() => {
    const email = searchParams.get("email");
    const token = searchParams.get("token");
    if (email && token && !autoSubmitted.current) {
      autoSubmitted.current = true;
      confirmMutation.mutate({ email, token });
    }
  }, [confirmMutation, searchParams]);

  return (
    <section className="auth-shell">
      <div className="panel auth-card">
        <span className="eyebrow">Email Verification</span>
        <h1>Xác thực tài khoản để bắt đầu mua sắm.</h1>
        <p className="muted-copy">
          Nếu bạn mở từ email của 2BShop, hệ thống sẽ tự xác thực. Bạn cũng có thể nhập lại email và mã token thủ công.
        </p>

        {confirmMutation.isSuccess ? (
          <div className="inline-alert inline-alert-success">
            Xác thực email thành công. Bạn có thể đăng nhập ngay bây giờ.
          </div>
        ) : null}
        {confirmMutation.isError ? (
          <div className="inline-alert inline-alert-danger">{getErrorMessage(confirmMutation.error)}</div>
        ) : null}

        <form className="form-grid" onSubmit={form.handleSubmit((values) => confirmMutation.mutate(values))}>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="email">Email</label>
            <input className="field" id="email" type="email" {...form.register("email", { required: true })} />
          </div>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="token">Mã xác thực</label>
            <input className="field" id="token" {...form.register("token", { required: true })} />
          </div>
          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <a className="muted-copy" href="/login">
              Quay lại đăng nhập
            </a>
            <button className="button button-primary" disabled={confirmMutation.isPending} type="submit">
              {confirmMutation.isPending ? "Đang xác thực..." : "Xác thực email"}
            </button>
          </div>
        </form>
      </div>
    </section>
  );
}

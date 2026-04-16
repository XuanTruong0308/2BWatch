import { useMutation, useQuery } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useSearchParams } from "react-router-dom";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson } from "@/lib/api/client";
import { useI18n } from "@/lib/i18n";
import type { ApiResponse } from "@/lib/api/types";
import { getErrorMessage } from "@/lib/utils/format";

type ResetValues = {
  newPassword: string;
  confirmPassword: string;
};

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "";
  const form = useForm<ResetValues>();
  const { tx } = useI18n();

  const validateQuery = useQuery({
    queryKey: ["auth", "reset-token", token],
    enabled: Boolean(token),
    queryFn: async () => {
      const response = await getJson<ApiResponse<{ valid: boolean }>>(
        `/api/v1/auth/reset-password/validate?token=${encodeURIComponent(token)}`,
      );
      return response.data.valid;
    },
  });

  const mutation = useMutation({
    mutationFn: async (values: ResetValues) =>
      postJson("/api/v1/auth/reset-password", {
        token,
        newPassword: values.newPassword,
        confirmPassword: values.confirmPassword,
      }),
  });

  if (!token) {
    return (
      <section className="auth-shell">
        <div className="panel auth-card">
          <div className="inline-alert inline-alert-danger">{tx("Liên kết đặt lại mật khẩu không hợp lệ.", "The reset link is missing a valid token.")}</div>
          <a className="button button-primary" href="/forgot-password">
            {tx("Yeu cau lien ket moi", "Request a new link")}
          </a>
        </div>
      </section>
    );
  }

  if (validateQuery.isLoading) {
    return <LoadingScreen label={tx("Đang xác minh liên kết đặt lại mật khẩu...", "Validating reset link...")} />;
  }

  if (validateQuery.isError || validateQuery.data === false) {
    return (
      <section className="auth-shell">
        <div className="panel auth-card">
          <div className="inline-alert inline-alert-danger">{tx("Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.", "This reset token is invalid or has expired.")}</div>
          <a className="button button-primary" href="/forgot-password">
            {tx("Gửi lại yêu cầu", "Send another request")}
          </a>
        </div>
      </section>
    );
  }

  return (
    <section className="auth-shell">
      <div className="panel auth-card">
        <span className="eyebrow">{tx("Dat lai mật khẩu", "Reset password")}</span>
        <h1>{tx("Tạo mật khẩu mới cho tài khoản của bạn.", "Create a new password for your account.")}</h1>
        <p className="muted-copy">{tx("Chọn mật khẩu mới và lưu lại theo đúng rule đang có của backend.", "Choose a stronger password that still follows the backend validation rules already in place.")}</p>

        {mutation.isSuccess ? (
          <div className="inline-alert inline-alert-success">
            {tx("Mật khẩu đã được cập nhật. Bạn có thể đăng nhập bằng mật khẩu mới.", "Your password has been updated. You can now sign in with the new password.")}
          </div>
        ) : null}
        {mutation.isError ? <div className="inline-alert inline-alert-danger">{getErrorMessage(mutation.error)}</div> : null}

        <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="newPassword">{tx("Mật khẩu moi", "New password")}</label>
            <input className="field" id="newPassword" type="password" {...form.register("newPassword", { required: true })} />
          </div>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="confirmPassword">{tx("Xác nhận mật khẩu", "Confirm password")}</label>
            <input
              className="field"
              id="confirmPassword"
              type="password"
              {...form.register("confirmPassword", { required: true })}
            />
          </div>
          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <a className="muted-copy" href="/login">
              {tx("Quay lại đăng nhập", "Back to sign in")}
            </a>
            <button className="button button-primary" disabled={mutation.isPending} type="submit">
              {mutation.isPending ? tx("Đang cập nhật...", "Saving...") : tx("Luu mật khẩu moi", "Save new password")}
            </button>
          </div>
        </form>
      </div>
    </section>
  );
}

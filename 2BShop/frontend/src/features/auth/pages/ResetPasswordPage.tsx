import { useMutation, useQuery } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useSearchParams } from "react-router-dom";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson } from "@/lib/api/client";
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
          <div className="inline-alert inline-alert-danger">Liên kết đặt lại mật khẩu không hợp lệ hoặc bị thiếu token.</div>
          <a className="button button-primary" href="/forgot-password">
            Yêu cầu liên kết mới
          </a>
        </div>
      </section>
    );
  }

  if (validateQuery.isLoading) {
    return <LoadingScreen label="Đang xác minh liên kết đặt lại mật khẩu..." />;
  }

  if (validateQuery.isError || validateQuery.data === false) {
    return (
      <section className="auth-shell">
        <div className="panel auth-card">
          <div className="inline-alert inline-alert-danger">Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.</div>
          <a className="button button-primary" href="/forgot-password">
            Gửi lại yêu cầu
          </a>
        </div>
      </section>
    );
  }

  return (
    <section className="auth-shell">
      <div className="panel auth-card">
        <span className="eyebrow">Reset Password</span>
        <h1>Tạo mật khẩu mới cho tài khoản của bạn.</h1>
        <p className="muted-copy">Mật khẩu nên có chữ hoa, chữ thường và số để đáp ứng đúng rule đang áp dụng ở backend.</p>

        {mutation.isSuccess ? (
          <div className="inline-alert inline-alert-success">
            Đổi mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.
          </div>
        ) : null}
        {mutation.isError ? <div className="inline-alert inline-alert-danger">{getErrorMessage(mutation.error)}</div> : null}

        <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="newPassword">Mật khẩu mới</label>
            <input className="field" id="newPassword" type="password" {...form.register("newPassword", { required: true })} />
          </div>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="confirmPassword">Xác nhận mật khẩu</label>
            <input
              className="field"
              id="confirmPassword"
              type="password"
              {...form.register("confirmPassword", { required: true })}
            />
          </div>
          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <a className="muted-copy" href="/login">
              Quay lại đăng nhập
            </a>
            <button className="button button-primary" disabled={mutation.isPending} type="submit">
              {mutation.isPending ? "Đang cập nhật..." : "Lưu mật khẩu mới"}
            </button>
          </div>
        </form>
      </div>
    </section>
  );
}

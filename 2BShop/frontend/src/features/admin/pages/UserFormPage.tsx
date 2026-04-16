import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson, putJson } from "@/lib/api/client";
import type { ApiResponse, User, UserOptionsPayload } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage } from "@/lib/utils/format";

type UserValues = {
  username: string;
  email: string;
  fullName: string;
  phone: string;
  address: string;
  avatarUrl: string;
  enabled: boolean;
  newPassword: string;
  roleNames: string[];
};

const roleLabel = (role: string, tx: (vi: string, en: string) => string) => {
  switch (role) {
    case "ADMIN":
      return tx("Quản trị vien", "Administrator");
    case "USER":
      return tx("Người dùng", "User");
    default:
      return role;
  }
};

export default function UserFormPage() {
  const { tx } = useI18n();
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const form = useForm<UserValues>({
    defaultValues: {
      username: "",
      email: "",
      fullName: "",
      phone: "",
      address: "",
      avatarUrl: "",
      enabled: true,
      newPassword: "",
      roleNames: ["USER"],
    },
  });

  const optionsQuery = useQuery({
    queryKey: ["admin", "user-options"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<UserOptionsPayload>>("/api/v1/admin/users/options");
      return response.data;
    },
  });

  const detailQuery = useQuery({
    queryKey: ["admin", "user", id],
    enabled: editing,
    queryFn: async () => {
      const response = await getJson<ApiResponse<User>>(`/api/v1/admin/users/${id}`);
      return response.data;
    },
  });

  useEffect(() => {
    if (!detailQuery.data) {
      return;
    }
    form.reset({
      username: detailQuery.data.username,
      email: detailQuery.data.email,
      fullName: detailQuery.data.fullName || "",
      phone: detailQuery.data.phone || "",
      address: detailQuery.data.address || "",
      avatarUrl: detailQuery.data.avatarUrl || "",
      enabled: Boolean(detailQuery.data.enabled),
      newPassword: "",
      roleNames: detailQuery.data.roles,
    });
  }, [detailQuery.data, form]);

  const mutation = useMutation({
    mutationFn: async (values: UserValues) => {
      if (editing) {
        return putJson(`/api/v1/admin/users/${id}`, values);
      }
      return postJson("/api/v1/admin/users", values);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      navigate("/admin/users");
    },
  });

  if (optionsQuery.isLoading || detailQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải form người dùng...", "Loading user form...")} />;
  }

  if (optionsQuery.isError || !optionsQuery.data || detailQuery.isError) {
    return <ErrorState message={tx("Không thể tải dữ liệu người dùng.", "Could not load user data.")} />;
  }

  const selectedRoles = form.watch("roleNames") || [];

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Form người dùng", "User form")}</span>
          <h2>{editing ? tx("Chỉnh sửa người dùng", "Edit user") : tx("Tạo người dùng mới", "Create new user")}</h2>
        </div>
        <Link className="button button-subtle" to="/admin/users">
          {tx("Quay lại danh sách", "Back to list")}
        </Link>
      </div>

      <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
        <div className="field-group">
          <label htmlFor="username">Username</label>
          <input className="field" id="username" {...form.register("username", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="email">Email</label>
          <input className="field" id="email" type="email" {...form.register("email", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="fullName">{tx("Ho va ten", "Full name")}</label>
          <input className="field" id="fullName" {...form.register("fullName")} />
        </div>
        <div className="field-group">
          <label htmlFor="phone">{tx("Số điện thoại", "Phone")}</label>
          <input className="field" id="phone" {...form.register("phone")} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label htmlFor="address">{tx("Địa chỉ", "Address")}</label>
          <textarea className="textarea" id="address" rows={3} {...form.register("address")} />
        </div>
        <div className="field-group">
          <label htmlFor="avatarUrl">{tx("Duong dan avatar", "Avatar URL")}</label>
          <input className="field" id="avatarUrl" {...form.register("avatarUrl")} />
        </div>
        <div className="field-group">
          <label htmlFor="newPassword">{editing ? tx("Mật khẩu moi (neu doi)", "New password (optional)") : tx("Mật khẩu", "Password")}</label>
          <input className="field" id="newPassword" type="password" {...form.register("newPassword")} />
        </div>

        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label>{tx("Vai tro", "Roles")}</label>
          <div className="choice-grid">
            {optionsQuery.data.roles.map((role) => (
              <label className="choice-card" key={role.value}>
                <input
                  checked={selectedRoles.includes(role.value)}
                  onChange={(event) => {
                    const nextRoles = event.target.checked
                      ? [...selectedRoles, role.value]
                      : selectedRoles.filter((value) => value !== role.value);
                    form.setValue("roleNames", nextRoles.length ? nextRoles : ["USER"]);
                  }}
                  type="checkbox"
                />
                <div>
                  <strong>{roleLabel(role.value, tx)}</strong>
                </div>
              </label>
            ))}
          </div>
        </div>

        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label className="checkbox-row">
            <input type="checkbox" {...form.register("enabled")} />
            <span>{tx("Tài khoản đang hoạt động", "Account is active")}</span>
          </label>
        </div>

        {mutation.isError ? <p className="inline-text-error">{getErrorMessage(mutation.error)}</p> : null}

        <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
          <button className="button button-primary" disabled={mutation.isPending} type="submit">
            {mutation.isPending
              ? tx("Đang lưu...", "Saving...")
              : editing
                ? tx("Luu thay doi", "Save changes")
                : tx("Tạo người dùng", "Create user")}
          </button>
        </div>
      </form>
    </div>
  );
}

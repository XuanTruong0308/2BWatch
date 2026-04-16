import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useSearchParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postFormData, postJson, putJson } from "@/lib/api/client";
import type { ApiResponse, AuthUser } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage } from "@/lib/utils/format";

type ProfileValues = {
  fullName: string;
  phone: string;
  address: string;
};

type PhoneValues = {
  phone: string;
};

type PasswordValues = {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
};

export default function ProfilePage() {
  const { tx } = useI18n();
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = searchParams.get("tab") || "profile";
  const queryClient = useQueryClient();
  const [avatarFile, setAvatarFile] = useState<File | null>(null);

  const profileForm = useForm<ProfileValues>();
  const phoneForm = useForm<PhoneValues>();
  const passwordForm = useForm<PasswordValues>();

  const profileQuery = useQuery({
    queryKey: ["profile"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<AuthUser>>("/api/v1/profile/me");
      return response.data;
    },
  });

  useEffect(() => {
    if (!profileQuery.data) {
      return;
    }
    profileForm.reset({
      fullName: profileQuery.data.fullName || "",
      phone: profileQuery.data.phone || "",
      address: profileQuery.data.address || "",
    });
    phoneForm.reset({
      phone: profileQuery.data.phone || "",
    });
  }, [phoneForm, profileForm, profileQuery.data]);

  const avatarPreview = useMemo(() => {
    if (avatarFile) {
      return URL.createObjectURL(avatarFile);
    }
    return profileQuery.data?.avatarUrl || "";
  }, [avatarFile, profileQuery.data?.avatarUrl]);

  const refreshProfile = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ["profile"] }),
      queryClient.invalidateQueries({ queryKey: ["auth", "me"] }),
    ]);
  };

  const updateProfile = useMutation({
    mutationFn: async (values: ProfileValues) => putJson("/api/v1/profile", values),
    onSuccess: refreshProfile,
  });

  const updatePhone = useMutation({
    mutationFn: async (values: PhoneValues) => postJson("/api/v1/profile/phone", values),
    onSuccess: refreshProfile,
  });

  const uploadAvatar = useMutation({
    mutationFn: async () => {
      if (!avatarFile) {
        throw new Error(tx("Vui long chon ảnh đại diện truoc.", "Please choose an avatar image first."));
      }
      const formData = new FormData();
      formData.append("avatar", avatarFile);
      return postFormData("/api/v1/profile/avatar", formData);
    },
    onSuccess: async () => {
      setAvatarFile(null);
      await refreshProfile();
    },
  });

  const changePassword = useMutation({
    mutationFn: async (values: PasswordValues) => postJson("/api/v1/profile/change-password", values),
    onSuccess: () => passwordForm.reset(),
  });

  if (profileQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải hồ sơ tài khoản...", "Loading account profile...")} />;
  }

  if (profileQuery.isError || !profileQuery.data) {
    return <ErrorState message={tx("Không thể tải hồ sơ của bạn.", "We could not load your profile.")} />;
  }

  const profile = profileQuery.data;

  return (
    <div className="split-grid">
      <section className="panel">
        <span className="eyebrow">{tx("Tài khoản", "Account")}</span>
        <h1>{tx("Hồ sơ và bảo mật", "Profile and security")}</h1>
        <p className="muted-copy">
          {tx(
            "Quản lý thông tin cá nhân, ảnh đại diện, xác minh số điện thoại và mật khẩu trong cùng một khu vực tài khoản.",
            "Manage profile data, avatar, phone verification and password from a single monochrome account surface.",
          )}
        </p>

        <div className="tab-pills">
          {[
            { value: "profile", label: tx("Hồ sơ", "Profile") },
            { value: "security", label: tx("Bảo mật", "Security") },
          ].map((tab) => (
            <button
              className={`button ${tab.value === activeTab ? "button-primary" : "button-subtle"}`}
              key={tab.value}
              onClick={() => {
                const next = new URLSearchParams(searchParams);
                next.set("tab", tab.value);
                setSearchParams(next);
              }}
              type="button"
            >
              {tab.label}
            </button>
          ))}
        </div>

        {activeTab === "profile" ? (
          <div className="stack-section">
            <div className="profile-hero">
              <div className="profile-avatar-xl">
                {avatarPreview ? <img alt={profile.fullName || profile.email || tx("Ảnh đại diện", "Avatar")} src={avatarPreview} /> : <span>2B</span>}
              </div>
              <div>
                <h3>{profile.fullName || profile.email}</h3>
                <p className="muted-copy">{profile.email}</p>
                <p className="muted-copy">
                  {tx("Đăng nhập bởi:", "Provider:")} {profile.provider || "LOCAL"}
                </p>
              </div>
            </div>

            <div className="panel inner-panel">
              <h3>{tx("Ảnh đại diện", "Avatar")}</h3>
              <div className="header-actions" style={{ justifyContent: "flex-start", alignItems: "end", flexWrap: "wrap", marginTop: 16 }}>
                <div className="field-group" style={{ minWidth: 260 }}>
                  <label htmlFor="avatar">{tx("Chọn ảnh", "Choose image")}</label>
                  <input
                    className="field"
                    id="avatar"
                    onChange={(event) => setAvatarFile(event.target.files?.[0] || null)}
                    type="file"
                  />
                </div>
                <button className="button button-primary" disabled={uploadAvatar.isPending || !avatarFile} onClick={() => uploadAvatar.mutate()} type="button">
                  {uploadAvatar.isPending ? tx("Đang tải lên...", "Uploading...") : tx("Cập nhật ảnh đại diện", "Update avatar")}
                </button>
              </div>
              {uploadAvatar.isError ? <p className="inline-text-error">{getErrorMessage(uploadAvatar.error)}</p> : null}
            </div>

            <form className="panel inner-panel form-grid" onSubmit={profileForm.handleSubmit((values) => updateProfile.mutate(values))}>
              <div className="field-group">
                <label htmlFor="fullName">{tx("Họ và tên", "Full name")}</label>
                <input className="field" id="fullName" {...profileForm.register("fullName", { required: true })} />
              </div>
              <div className="field-group">
                <label htmlFor="phone">{tx("Số điện thoại", "Phone")}</label>
                <input className="field" id="phone" {...profileForm.register("phone")} />
              </div>
              <div className="field-group" style={{ gridColumn: "1 / -1" }}>
                <label htmlFor="address">{tx("Địa chỉ", "Address")}</label>
                <textarea className="textarea" id="address" rows={4} {...profileForm.register("address")} />
              </div>
              {updateProfile.isError ? <p className="inline-text-error">{getErrorMessage(updateProfile.error)}</p> : null}
              {updateProfile.isSuccess ? <p className="inline-text-success">{tx("Cập nhật hồ sơ thành công.", "Profile details were updated successfully.")}</p> : null}
              <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
                <button className="button button-primary" disabled={updateProfile.isPending} type="submit">
                  {updateProfile.isPending ? tx("Đang lưu...", "Saving...") : tx("Lưu thay đổi", "Save changes")}
                </button>
              </div>
            </form>

            {profile.provider === "GOOGLE" ? (
              <form className="panel inner-panel form-grid" onSubmit={phoneForm.handleSubmit((values) => updatePhone.mutate(values))}>
                <div className="field-group" style={{ gridColumn: "1 / -1" }}>
                  <label htmlFor="verifiedPhone">{tx("Số điện thoại để xác minh đơn hàng", "Phone number for order verification")}</label>
                  <input className="field" id="verifiedPhone" {...phoneForm.register("phone", { required: true })} />
                </div>
                {updatePhone.isError ? <p className="inline-text-error">{getErrorMessage(updatePhone.error)}</p> : null}
                {updatePhone.isSuccess ? <p className="inline-text-success">{tx("Đã cập nhật và xác minh số điện thoại.", "Phone number updated and verified.")}</p> : null}
                <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
                  <span className="muted-copy">
                    {tx("Xác minh:", "Verification:")} {profile.phoneVerified ? tx("Đã xác minh", "Verified") : tx("Chưa xác minh", "Not verified")}
                  </span>
                  <button className="button button-subtle" disabled={updatePhone.isPending} type="submit">
                    {tx("Cập nhật số điện thoại", "Update phone")}
                  </button>
                </div>
              </form>
            ) : null}
          </div>
        ) : (
          <form className="panel inner-panel form-grid" onSubmit={passwordForm.handleSubmit((values) => changePassword.mutate(values))}>
            <div className="field-group" style={{ gridColumn: "1 / -1" }}>
              <label htmlFor="currentPassword">{tx("Mật khẩu hiện tại", "Current password")}</label>
              <input className="field" id="currentPassword" type="password" {...passwordForm.register("currentPassword", { required: true })} />
            </div>
            <div className="field-group">
              <label htmlFor="newPassword">{tx("Mật khẩu mới", "New password")}</label>
              <input className="field" id="newPassword" type="password" {...passwordForm.register("newPassword", { required: true })} />
            </div>
            <div className="field-group">
              <label htmlFor="confirmPassword">{tx("Xác nhận mật khẩu mới", "Confirm new password")}</label>
              <input className="field" id="confirmPassword" type="password" {...passwordForm.register("confirmPassword", { required: true })} />
            </div>
            {changePassword.isError ? <p className="inline-text-error">{getErrorMessage(changePassword.error)}</p> : null}
            {changePassword.isSuccess ? <p className="inline-text-success">{tx("Đổi mật khẩu thành công.", "Password changed successfully.")}</p> : null}
            <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
              <button className="button button-primary" disabled={changePassword.isPending} type="submit">
                {changePassword.isPending ? tx("Đang cập nhật...", "Updating...") : tx("Đổi mật khẩu", "Change password")}
              </button>
            </div>
          </form>
        )}
      </section>

      <aside className="panel">
        <span className="eyebrow">{tx("Tổng quan", "Snapshot")}</span>
        <h2>{tx("Trạng thái tài khoản", "Account status")}</h2>
        <div className="summary-list">
          <div className="summary-row">
            <span>{tx("Email", "Email")}</span>
            <strong>{profile.email}</strong>
          </div>
          <div className="summary-row">
            <span>{tx("Vai trò", "Roles")}</span>
            <strong>{profile.roles.join(", ") || "USER"}</strong>
          </div>
          <div className="summary-row">
            <span>{tx("Email đã xác minh", "Email verified")}</span>
            <strong>{profile.emailVerified ? tx("Đã xác minh", "Verified") : tx("Chưa xác minh", "Not verified")}</strong>
          </div>
          <div className="summary-row">
            <span>{tx("Số điện thoại", "Phone")}</span>
            <strong>{profile.phone || tx("Chưa cập nhật", "Not updated")}</strong>
          </div>
          <div className="summary-row">
            <span>{tx("Trạng thái", "Status")}</span>
            <strong>{profile.enabled ? tx("Đang hoạt động", "Active") : tx("Đã khóa", "Locked")}</strong>
          </div>
        </div>
      </aside>
    </div>
  );
}

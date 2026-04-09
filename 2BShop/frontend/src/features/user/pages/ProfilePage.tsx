import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useSearchParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postFormData, postJson, putJson } from "@/lib/api/client";
import type { ApiResponse, AuthUser } from "@/lib/api/types";
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
        throw new Error("Vui lòng chọn ảnh đại diện.");
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
    return <LoadingScreen label="Đang tải thông tin tài khoản..." />;
  }

  if (profileQuery.isError || !profileQuery.data) {
    return <ErrorState message="Không thể tải thông tin tài khoản." />;
  }

  const profile = profileQuery.data;

  return (
    <div className="split-grid">
      <section className="panel">
        <span className="eyebrow">Account</span>
        <h1>Thông tin cá nhân</h1>
        <p className="muted-copy">Quản lý hồ sơ, ảnh đại diện, số điện thoại và mật khẩu từ một giao diện thống nhất hơn.</p>

        <div className="tab-pills">
          {[
            { value: "profile", label: "Hồ sơ" },
            { value: "security", label: "Bảo mật" },
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
                {avatarPreview ? <img alt={profile.fullName || profile.email || "Avatar"} src={avatarPreview} /> : <span>2B</span>}
              </div>
              <div>
                <h3>{profile.fullName || profile.email}</h3>
                <p className="muted-copy">{profile.email}</p>
                <p className="muted-copy">Provider: {profile.provider || "LOCAL"}</p>
              </div>
            </div>

            <div className="panel inner-panel">
              <h3>Ảnh đại diện</h3>
              <div className="header-actions" style={{ justifyContent: "flex-start", alignItems: "end", flexWrap: "wrap" }}>
                <div className="field-group" style={{ minWidth: 260 }}>
                  <label htmlFor="avatar">Chọn ảnh</label>
                  <input
                    className="field"
                    id="avatar"
                    onChange={(event) => setAvatarFile(event.target.files?.[0] || null)}
                    type="file"
                  />
                </div>
                <button className="button button-primary" disabled={uploadAvatar.isPending || !avatarFile} onClick={() => uploadAvatar.mutate()} type="button">
                  {uploadAvatar.isPending ? "Đang tải ảnh..." : "Cập nhật ảnh"}
                </button>
              </div>
              {uploadAvatar.isError ? <p className="inline-text-error">{getErrorMessage(uploadAvatar.error)}</p> : null}
            </div>

            <form className="panel inner-panel form-grid" onSubmit={profileForm.handleSubmit((values) => updateProfile.mutate(values))}>
              <div className="field-group">
                <label htmlFor="fullName">Họ và tên</label>
                <input className="field" id="fullName" {...profileForm.register("fullName", { required: true })} />
              </div>
              <div className="field-group">
                <label htmlFor="phone">Số điện thoại</label>
                <input className="field" id="phone" {...profileForm.register("phone")} />
              </div>
              <div className="field-group" style={{ gridColumn: "1 / -1" }}>
                <label htmlFor="address">Địa chỉ</label>
                <textarea className="textarea" id="address" rows={4} {...profileForm.register("address")} />
              </div>
              {updateProfile.isError ? <p className="inline-text-error">{getErrorMessage(updateProfile.error)}</p> : null}
              {updateProfile.isSuccess ? <p className="inline-text-success">Thông tin hồ sơ đã được cập nhật.</p> : null}
              <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
                <button className="button button-primary" disabled={updateProfile.isPending} type="submit">
                  {updateProfile.isPending ? "Đang lưu..." : "Lưu thay đổi"}
                </button>
              </div>
            </form>

            {profile.provider === "GOOGLE" ? (
              <form className="panel inner-panel form-grid" onSubmit={phoneForm.handleSubmit((values) => updatePhone.mutate(values))}>
                <div className="field-group" style={{ gridColumn: "1 / -1" }}>
                  <label htmlFor="verifiedPhone">Cập nhật số điện thoại để xác minh đơn hàng</label>
                  <input className="field" id="verifiedPhone" {...phoneForm.register("phone", { required: true })} />
                </div>
                {updatePhone.isError ? <p className="inline-text-error">{getErrorMessage(updatePhone.error)}</p> : null}
                {updatePhone.isSuccess ? <p className="inline-text-success">Số điện thoại đã được cập nhật và xác minh.</p> : null}
                <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
                  <span className="muted-copy">
                    Trạng thái xác minh: {profile.phoneVerified ? "Đã xác minh" : "Chưa xác minh"}
                  </span>
                  <button className="button button-subtle" disabled={updatePhone.isPending} type="submit">
                    Cập nhật số điện thoại
                  </button>
                </div>
              </form>
            ) : null}
          </div>
        ) : (
          <form className="panel inner-panel form-grid" onSubmit={passwordForm.handleSubmit((values) => changePassword.mutate(values))}>
            <div className="field-group" style={{ gridColumn: "1 / -1" }}>
              <label htmlFor="currentPassword">Mật khẩu hiện tại</label>
              <input className="field" id="currentPassword" type="password" {...passwordForm.register("currentPassword", { required: true })} />
            </div>
            <div className="field-group">
              <label htmlFor="newPassword">Mật khẩu mới</label>
              <input className="field" id="newPassword" type="password" {...passwordForm.register("newPassword", { required: true })} />
            </div>
            <div className="field-group">
              <label htmlFor="confirmPassword">Xác nhận mật khẩu mới</label>
              <input
                className="field"
                id="confirmPassword"
                type="password"
                {...passwordForm.register("confirmPassword", { required: true })}
              />
            </div>
            {changePassword.isError ? <p className="inline-text-error">{getErrorMessage(changePassword.error)}</p> : null}
            {changePassword.isSuccess ? <p className="inline-text-success">Mật khẩu đã được thay đổi thành công.</p> : null}
            <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
              <button className="button button-primary" disabled={changePassword.isPending} type="submit">
                {changePassword.isPending ? "Đang cập nhật..." : "Đổi mật khẩu"}
              </button>
            </div>
          </form>
        )}
      </section>

      <aside className="panel">
        <span className="eyebrow">Snapshot</span>
        <h2>Tình trạng tài khoản</h2>
        <div className="summary-list">
          <div className="summary-row">
            <span>Email</span>
            <strong>{profile.email}</strong>
          </div>
          <div className="summary-row">
            <span>Vai trò</span>
            <strong>{profile.roles.join(", ") || "USER"}</strong>
          </div>
          <div className="summary-row">
            <span>Email xác thực</span>
            <strong>{profile.emailVerified ? "Đã xác thực" : "Chưa xác thực"}</strong>
          </div>
          <div className="summary-row">
            <span>Số điện thoại</span>
            <strong>{profile.phone || "Chưa cập nhật"}</strong>
          </div>
          <div className="summary-row">
            <span>Trạng thái</span>
            <strong>{profile.enabled ? "Đang hoạt động" : "Bị khóa"}</strong>
          </div>
        </div>
      </aside>
    </div>
  );
}

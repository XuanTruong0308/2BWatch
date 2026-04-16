import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson, putJson } from "@/lib/api/client";
import type { ApiResponse, Brand } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage } from "@/lib/utils/format";

type BrandValues = {
  brandName: string;
  description: string;
  logoUrl: string;
  active: boolean;
};

export default function BrandFormPage() {
  const { tx } = useI18n();
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const editing = Boolean(id);
  const form = useForm<BrandValues>({
    defaultValues: {
      brandName: "",
      description: "",
      logoUrl: "",
      active: true,
    },
  });

  const detailQuery = useQuery({
    queryKey: ["admin", "brand", id],
    enabled: editing,
    queryFn: async () => {
      const response = await getJson<ApiResponse<Brand>>(`/api/v1/admin/brands/${id}`);
      return response.data;
    },
  });

  useEffect(() => {
    if (!detailQuery.data) {
      return;
    }
    form.reset({
      brandName: detailQuery.data.brandName,
      description: detailQuery.data.description || "",
      logoUrl: detailQuery.data.logoUrl || "",
      active: Boolean(detailQuery.data.active),
    });
  }, [detailQuery.data, form]);

  const mutation = useMutation({
    mutationFn: async (values: BrandValues) => {
      if (editing) {
        return putJson(`/api/v1/admin/brands/${id}`, values);
      }
      return postJson("/api/v1/admin/brands", values);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "brands"] });
      navigate("/admin/brands");
    },
  });

  if (detailQuery.isLoading) {
    return <LoadingScreen label={tx("Đang tải thong tin thương hiệu...", "Loading brand details...")} />;
  }

  if (detailQuery.isError) {
    return <ErrorState message={tx("Không thể tải dữ liệu thương hiệu de chinh sua.", "Could not load brand data for editing.")} />;
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Form thương hiệu", "Brand form")}</span>
          <h2>{editing ? tx("Chỉnh sửa thương hiệu", "Edit brand") : tx("Tạo thương hiệu mới", "Create new brand")}</h2>
        </div>
        <Link className="button button-subtle" to="/admin/brands">
          {tx("Quay lại danh sách", "Back to list")}
        </Link>
      </div>

      <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
        <div className="field-group">
          <label htmlFor="brandName">{tx("Tên thương hiệu", "Brand name")}</label>
          <input className="field" id="brandName" {...form.register("brandName", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="logoUrl">{tx("Duong dan logo", "Logo URL")}</label>
          <input className="field" id="logoUrl" {...form.register("logoUrl")} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label htmlFor="description">{tx("Mô tả", "Description")}</label>
          <textarea className="textarea" id="description" rows={5} {...form.register("description")} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label className="checkbox-row">
            <input type="checkbox" {...form.register("active")} />
            <span>{tx("Thương hiệu đang hoạt động", "Brand is active")}</span>
          </label>
        </div>

        {mutation.isError ? <p className="inline-text-error">{getErrorMessage(mutation.error)}</p> : null}

        <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
          <button className="button button-primary" disabled={mutation.isPending} type="submit">
            {mutation.isPending
              ? tx("Đang lưu...", "Saving...")
              : editing
                ? tx("Luu thay doi", "Save changes")
                : tx("Tạo thương hiệu", "Create brand")}
          </button>
        </div>
      </form>
    </div>
  );
}

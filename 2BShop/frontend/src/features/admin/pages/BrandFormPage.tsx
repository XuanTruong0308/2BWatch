import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson, putJson } from "@/lib/api/client";
import type { ApiResponse, Brand } from "@/lib/api/types";
import { getErrorMessage } from "@/lib/utils/format";

type BrandValues = {
  brandName: string;
  description: string;
  logoUrl: string;
  active: boolean;
};

export default function BrandFormPage() {
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
    return <LoadingScreen label="Đang tải thông tin brand..." />;
  }

  if (detailQuery.isError) {
    return <ErrorState message="Không thể tải dữ liệu brand để chỉnh sửa." />;
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Brand Form</span>
          <h2>{editing ? "Chỉnh sửa thương hiệu" : "Tạo thương hiệu mới"}</h2>
        </div>
        <Link className="button button-subtle" to="/admin/brands">
          Quay lại danh sách
        </Link>
      </div>

      <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
        <div className="field-group">
          <label htmlFor="brandName">Tên brand</label>
          <input className="field" id="brandName" {...form.register("brandName", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="logoUrl">Logo URL</label>
          <input className="field" id="logoUrl" {...form.register("logoUrl")} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label htmlFor="description">Mô tả</label>
          <textarea className="textarea" id="description" rows={5} {...form.register("description")} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label className="checkbox-row">
            <input type="checkbox" {...form.register("active")} />
            <span>Brand đang hoạt động</span>
          </label>
        </div>

        {mutation.isError ? <p className="inline-text-error">{getErrorMessage(mutation.error)}</p> : null}

        <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
          <button className="button button-primary" disabled={mutation.isPending} type="submit">
            {mutation.isPending ? "Đang lưu..." : editing ? "Lưu thay đổi" : "Tạo brand"}
          </button>
        </div>
      </form>
    </div>
  );
}

import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson, putJson } from "@/lib/api/client";
import type { ApiResponse, PaymentMethod } from "@/lib/api/types";
import { getErrorMessage } from "@/lib/utils/format";

type PaymentMethodValues = {
  methodName: string;
  description: string;
  active: boolean;
};

export default function PaymentMethodFormPage() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const form = useForm<PaymentMethodValues>({
    defaultValues: {
      methodName: "",
      description: "",
      active: true,
    },
  });

  const detailQuery = useQuery({
    queryKey: ["admin", "payment-method", id],
    enabled: editing,
    queryFn: async () => {
      const response = await getJson<ApiResponse<PaymentMethod>>(`/api/v1/admin/payments/methods/${id}`);
      return response.data;
    },
  });

  useEffect(() => {
    if (!detailQuery.data) {
      return;
    }
    form.reset({
      methodName: detailQuery.data.methodName,
      description: detailQuery.data.description || "",
      active: Boolean(detailQuery.data.active),
    });
  }, [detailQuery.data, form]);

  const mutation = useMutation({
    mutationFn: async (values: PaymentMethodValues) => {
      if (editing) {
        return putJson(`/api/v1/admin/payments/methods/${id}`, values);
      }
      return postJson("/api/v1/admin/payments/methods", values);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "payment-methods"] });
      navigate("/admin/payments/methods");
    },
  });

  if (detailQuery.isLoading) {
    return <LoadingScreen label="Đang tải phương thức thanh toán..." />;
  }

  if (detailQuery.isError) {
    return <ErrorState message="Không thể tải dữ liệu phương thức thanh toán." />;
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Payment Method Form</span>
          <h2>{editing ? "Chỉnh sửa phương thức" : "Tạo phương thức mới"}</h2>
        </div>
        <Link className="button button-subtle" to="/admin/payments/methods">
          Quay lại danh sách
        </Link>
      </div>

      <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
        <div className="field-group">
          <label htmlFor="methodName">Tên phương thức</label>
          <input className="field" id="methodName" {...form.register("methodName", { required: true })} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label htmlFor="description">Mô tả</label>
          <textarea className="textarea" id="description" rows={4} {...form.register("description")} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label className="checkbox-row">
            <input type="checkbox" {...form.register("active")} />
            <span>Phương thức đang hoạt động</span>
          </label>
        </div>

        {mutation.isError ? <p className="inline-text-error">{getErrorMessage(mutation.error)}</p> : null}

        <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
          <button className="button button-primary" disabled={mutation.isPending} type="submit">
            {mutation.isPending ? "Đang lưu..." : editing ? "Lưu thay đổi" : "Tạo phương thức"}
          </button>
        </div>
      </form>
    </div>
  );
}

import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson, putJson } from "@/lib/api/client";
import type { ApiResponse, PaymentMethod } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage } from "@/lib/utils/format";

type PaymentMethodValues = {
  methodName: string;
  description: string;
  active: boolean;
};

export default function PaymentMethodFormPage() {
  const { tx } = useI18n();
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
    return <LoadingScreen label={tx("Đang tải phương thức thanh toán...", "Loading payment method...")} />;
  }

  if (detailQuery.isError) {
    return <ErrorState message={tx("Không thể tải dữ liệu phương thức thanh toán.", "Could not load payment method data.")} />;
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Form phuong thuc", "Payment method form")}</span>
          <h2>{editing ? tx("Chỉnh sửa phương thức", "Edit method") : tx("Tạo phương thức mới", "Create new method")}</h2>
        </div>
        <Link className="button button-subtle" to="/admin/payments/methods">
          {tx("Quay lại danh sách", "Back to list")}
        </Link>
      </div>

      <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
        <div className="field-group">
          <label htmlFor="methodName">{tx("Ten phuong thuc", "Method name")}</label>
          <input className="field" id="methodName" {...form.register("methodName", { required: true })} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label htmlFor="description">{tx("Mô tả", "Description")}</label>
          <textarea className="textarea" id="description" rows={4} {...form.register("description")} />
        </div>
        <div className="field-group" style={{ gridColumn: "1 / -1" }}>
          <label className="checkbox-row">
            <input type="checkbox" {...form.register("active")} />
            <span>{tx("Phuong thuc đang hoạt động", "Method is active")}</span>
          </label>
        </div>

        {mutation.isError ? <p className="inline-text-error">{getErrorMessage(mutation.error)}</p> : null}

        <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
          <button className="button button-primary" disabled={mutation.isPending} type="submit">
            {mutation.isPending
              ? tx("Đang lưu...", "Saving...")
              : editing
                ? tx("Luu thay doi", "Save changes")
                : tx("Tạo phương thức", "Create method")}
          </button>
        </div>
      </form>
    </div>
  );
}

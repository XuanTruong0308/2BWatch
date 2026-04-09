import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson, putJson } from "@/lib/api/client";
import type { ApiResponse, BankAccount } from "@/lib/api/types";
import { getErrorMessage } from "@/lib/utils/format";

type BankAccountValues = {
  bankName: string;
  bankCode: string;
  accountNumber: string;
  accountHolder: string;
  active: boolean;
  displayOrder: number;
};

export default function BankAccountFormPage() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const form = useForm<BankAccountValues>({
    defaultValues: {
      bankName: "",
      bankCode: "",
      accountNumber: "",
      accountHolder: "",
      active: true,
      displayOrder: 0,
    },
  });

  const detailQuery = useQuery({
    queryKey: ["admin", "bank-account", id],
    enabled: editing,
    queryFn: async () => {
      const response = await getJson<ApiResponse<BankAccount>>(`/api/v1/admin/bank-accounts/${id}`);
      return response.data;
    },
  });

  useEffect(() => {
    if (!detailQuery.data) {
      return;
    }
    form.reset({
      bankName: detailQuery.data.bankName,
      bankCode: detailQuery.data.bankCode,
      accountNumber: detailQuery.data.accountNumber,
      accountHolder: detailQuery.data.accountHolder,
      active: Boolean(detailQuery.data.active),
      displayOrder: detailQuery.data.displayOrder || 0,
    });
  }, [detailQuery.data, form]);

  const mutation = useMutation({
    mutationFn: async (values: BankAccountValues) => {
      if (editing) {
        return putJson(`/api/v1/admin/bank-accounts/${id}`, values);
      }
      return postJson("/api/v1/admin/bank-accounts", values);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "bank-accounts"] });
      navigate("/admin/bank-accounts");
    },
  });

  if (detailQuery.isLoading) {
    return <LoadingScreen label="Đang tải tài khoản ngân hàng..." />;
  }

  if (detailQuery.isError) {
    return <ErrorState message="Không thể tải dữ liệu tài khoản ngân hàng." />;
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Bank Account Form</span>
          <h2>{editing ? "Chỉnh sửa tài khoản ngân hàng" : "Tạo tài khoản ngân hàng mới"}</h2>
        </div>
        <Link className="button button-subtle" to="/admin/bank-accounts">
          Quay lại danh sách
        </Link>
      </div>

      <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
        <div className="field-group">
          <label htmlFor="bankName">Tên ngân hàng</label>
          <input className="field" id="bankName" {...form.register("bankName", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="bankCode">Mã ngân hàng</label>
          <input className="field" id="bankCode" {...form.register("bankCode", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="accountNumber">Số tài khoản</label>
          <input className="field" id="accountNumber" {...form.register("accountNumber", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="accountHolder">Chủ tài khoản</label>
          <input className="field" id="accountHolder" {...form.register("accountHolder", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="displayOrder">Thứ tự hiển thị</label>
          <input className="field" id="displayOrder" type="number" {...form.register("displayOrder", { valueAsNumber: true })} />
        </div>
        <div className="field-group">
          <label className="checkbox-row">
            <input type="checkbox" {...form.register("active")} />
            <span>Tài khoản đang hoạt động</span>
          </label>
        </div>

        {detailQuery.data?.qrImageUrl ? (
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label>QR hiện tại</label>
            <img alt={detailQuery.data.bankName} className="bank-qr" src={detailQuery.data.qrImageUrl} />
          </div>
        ) : null}

        {mutation.isError ? <p className="inline-text-error">{getErrorMessage(mutation.error)}</p> : null}

        <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
          <button className="button button-primary" disabled={mutation.isPending} type="submit">
            {mutation.isPending ? "Đang lưu..." : editing ? "Lưu thay đổi" : "Tạo tài khoản"}
          </button>
        </div>
      </form>
    </div>
  );
}

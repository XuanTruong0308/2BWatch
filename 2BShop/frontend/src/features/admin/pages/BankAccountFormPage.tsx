import { useEffect } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson, putJson } from "@/lib/api/client";
import type { ApiResponse, BankAccount } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
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
  const { tx } = useI18n();
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
    return <LoadingScreen label={tx("Đang tải tài khoản ngân hàng...", "Loading bank account...")} />;
  }

  if (detailQuery.isError) {
    return <ErrorState message={tx("Không thể tải dữ liệu tài khoản ngân hàng.", "Could not load bank account data.")} />;
  }

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Form tài khoản ngân hàng", "Bank account form")}</span>
          <h2>{editing ? tx("Chỉnh sửa tài khoản ngân hàng", "Edit bank account") : tx("Tạo tài khoản ngân hàng mới", "Create new bank account")}</h2>
        </div>
        <Link className="button button-subtle" to="/admin/bank-accounts">
          {tx("Quay lại danh sách", "Back to list")}
        </Link>
      </div>

      <form className="form-grid" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
        <div className="field-group">
          <label htmlFor="bankName">{tx("Tên ngân hàng", "Bank name")}</label>
          <input className="field" id="bankName" {...form.register("bankName", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="bankCode">{tx("Mã ngân hàng", "Bank code")}</label>
          <input className="field" id="bankCode" {...form.register("bankCode", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="accountNumber">{tx("Số tài khoản", "Account number")}</label>
          <input className="field" id="accountNumber" {...form.register("accountNumber", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="accountHolder">{tx("Chủ tài khoản", "Account holder")}</label>
          <input className="field" id="accountHolder" {...form.register("accountHolder", { required: true })} />
        </div>
        <div className="field-group">
          <label htmlFor="displayOrder">{tx("Thứ tự hiển thị", "Display order")}</label>
          <input className="field" id="displayOrder" type="number" {...form.register("displayOrder", { valueAsNumber: true })} />
        </div>
        <div className="field-group">
          <label className="checkbox-row">
            <input type="checkbox" {...form.register("active")} />
            <span>{tx("Tài khoản đang hoạt động", "Account is active")}</span>
          </label>
        </div>

        {detailQuery.data?.qrImageUrl ? (
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label>{tx("QR hiện tại", "Current QR")}</label>
            <img alt={detailQuery.data.bankName} className="bank-qr" src={detailQuery.data.qrImageUrl} />
          </div>
        ) : null}

        {mutation.isError ? <p className="inline-text-error">{getErrorMessage(mutation.error)}</p> : null}

        <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "flex-end" }}>
          <button className="button button-primary" disabled={mutation.isPending} type="submit">
            {mutation.isPending
              ? tx("Đang lưu...", "Saving...")
              : editing
                ? tx("Lưu thay đổi", "Save changes")
                : tx("Tạo tài khoản", "Create account")}
          </button>
        </div>
      </form>
    </div>
  );
}

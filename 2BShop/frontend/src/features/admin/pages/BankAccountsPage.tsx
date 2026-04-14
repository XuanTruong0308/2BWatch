import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { deleteJson, getJson } from "@/lib/api/client";
import type { ApiResponse, BankAccount } from "@/lib/api/types";
import { getErrorMessage, toBooleanText } from "@/lib/utils/format";

export default function BankAccountsPage() {
  const queryClient = useQueryClient();
  const [currentPage, setCurrentPage] = useState(0);
  const itemsPerPage = 10;

  const accountsQuery = useQuery({
    queryKey: ["admin", "bank-accounts"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<BankAccount[]>>("/api/v1/admin/bank-accounts");
      return response.data;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: number) => deleteJson(`/api/v1/admin/bank-accounts/${id}`),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "bank-accounts"] });
    },
  });

  if (accountsQuery.isLoading) {
    return <LoadingScreen label="Đang tải tài khoản ngân hàng..." />;
  }

  if (accountsQuery.isError || !accountsQuery.data) {
    return <ErrorState message="Không thể tải tài khoản ngân hàng." />;
  }

  const totalPages = Math.ceil(accountsQuery.data.length / itemsPerPage);
  const currentAccounts = accountsQuery.data.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage);

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Bank Accounts</span>
          <h2>Tài khoản nhận chuyển khoản</h2>
        </div>
        <Link className="button button-primary" to="/admin/bank-accounts/new">
          Thêm tài khoản
        </Link>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Ngân hàng</th>
              <th>Số tài khoản</th>
              <th>Chủ tài khoản</th>
              <th>QR</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {currentAccounts.map((account) => (
              <tr key={account.bankAccountId}>
                <td>
                  <strong>{account.bankName}</strong>
                  <div className="muted-copy">{account.bankCode}</div>
                </td>
                <td>{account.accountNumber}</td>
                <td>{account.accountHolder}</td>
                <td>{account.qrImageUrl ? <img alt={account.bankName} className="bank-qr bank-qr--thumb" src={account.qrImageUrl} /> : "N/A"}</td>
                <td>
                  <Badge label={toBooleanText(account.active)} tone={account.active ? "success" : "danger"} />
                </td>
                <td>
                  <div className="header-actions" style={{ justifyContent: "flex-start" }}>
                    <Link className="button button-subtle" to={`/admin/bank-accounts/${account.bankAccountId}/edit`}>
                      Sửa
                    </Link>
                    <button className="button button-danger" onClick={() => deleteMutation.mutate(account.bankAccountId)} type="button">
                      Xóa
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <Pagination
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          totalPages={totalPages}
        />
      )}

      {deleteMutation.isError ? <p className="inline-text-error">{getErrorMessage(deleteMutation.error)}</p> : null}
    </div>
  );
}

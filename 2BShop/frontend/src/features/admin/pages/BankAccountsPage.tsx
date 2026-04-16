import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { Pagination } from "@/components/ui/Pagination";
import { deleteJson, getJson } from "@/lib/api/client";
import type { ApiResponse, BankAccount } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
import { getErrorMessage, toBooleanText } from "@/lib/utils/format";

export default function BankAccountsPage() {
  const { tx } = useI18n();
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
    return <LoadingScreen label={tx("Đang tải tài khoản ngan hang...", "Loading bank accounts...")} />;
  }

  if (accountsQuery.isError || !accountsQuery.data) {
    return <ErrorState message={tx("Không thể tải tài khoản ngan hang.", "Could not load bank accounts.")} />;
  }

  const totalPages = Math.ceil(accountsQuery.data.length / itemsPerPage);
  const currentAccounts = accountsQuery.data.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage);

  return (
    <div className="panel">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{tx("Tài khoản ngan hang", "Bank accounts")}</span>
          <h2>{tx("Tài khoản nhận chuyển khoản", "Transfer destination accounts")}</h2>
        </div>
        <Link className="button button-primary" to="/admin/bank-accounts/new">
          {tx("Thêm tài khoản", "Add account")}
        </Link>
      </div>

      <div className="data-card">
        <table className="data-table">
          <thead>
            <tr>
              <th>{tx("Ngân hàng", "Bank")}</th>
              <th>{tx("Số tài khoản", "Account number")}</th>
              <th>{tx("Chủ tài khoản", "Account holder")}</th>
              <th>QR</th>
              <th>{tx("Trạng thái", "Status")}</th>
              <th>{tx("Hành động", "Actions")}</th>
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
                <td>{account.qrImageUrl ? <img alt={account.bankName} className="bank-qr bank-qr--thumb" src={account.qrImageUrl} /> : tx("Không có", "N/A")}</td>
                <td>
                  <Badge label={toBooleanText(account.active)} tone={account.active ? "success" : "danger"} />
                </td>
                <td>
                  <div className="header-actions" style={{ justifyContent: "flex-start" }}>
                    <Link className="button button-subtle" to={`/admin/bank-accounts/${account.bankAccountId}/edit`}>
                      {tx("Sửa", "Edit")}
                    </Link>
                    <button className="button button-danger" onClick={() => deleteMutation.mutate(account.bankAccountId)} type="button">
                      {tx("Xóa", "Delete")}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 ? <Pagination currentPage={currentPage} onPageChange={setCurrentPage} totalPages={totalPages} /> : null}

      {deleteMutation.isError ? <p className="inline-text-error">{getErrorMessage(deleteMutation.error)}</p> : null}
    </div>
  );
}

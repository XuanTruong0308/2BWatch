import { useEffect, useMemo, useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, CheckoutContext, Order } from "@/lib/api/types";
import { formatCurrency, getErrorMessage } from "@/lib/utils/format";

type CheckoutFormValues = {
  receiverName: string;
  phone: string;
  address: string;
  notes: string;
  paymentMethod: string;
  bankAccountId: string;
};

export default function CheckoutPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const [couponDraft, setCouponDraft] = useState(searchParams.get("couponCode") || "");
  const couponCode = searchParams.get("couponCode") || "";
  const formInitialized = useRef(false);

  const form = useForm<CheckoutFormValues>({
    defaultValues: {
      receiverName: "",
      phone: "",
      address: "",
      notes: "",
      paymentMethod: "",
      bankAccountId: "",
    },
  });

  const checkoutQuery = useQuery({
    queryKey: ["checkout", couponCode],
    queryFn: async () => {
      const query = couponCode ? `?couponCode=${encodeURIComponent(couponCode)}` : "";
      const response = await getJson<ApiResponse<CheckoutContext>>(`/api/v1/checkout${query}`);
      return response.data;
    },
  });

  const placeOrder = useMutation({
    mutationFn: async (values: CheckoutFormValues) =>
      postJson<ApiResponse<Order>>("/api/v1/checkout/place-order", {
        receiverName: values.receiverName,
        phone: values.phone,
        address: values.address,
        paymentMethod: values.paymentMethod,
        notes: values.notes,
        couponCode: couponCode || null,
        bankAccountId: values.paymentMethod === "BANK_TRANSFER" && values.bankAccountId ? Number(values.bankAccountId) : null,
      }),
    onSuccess: async (response) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["cart"] }),
        queryClient.invalidateQueries({ queryKey: ["cart", "count"] }),
        queryClient.invalidateQueries({ queryKey: ["orders"] }),
      ]);
      navigate(`/checkout/confirmation/${response.data.orderId}`);
    },
  });

  useEffect(() => {
    if (!checkoutQuery.data) {
      return;
    }

    const defaultMethod = checkoutQuery.data.paymentMethods.find((method) => method.active !== false)?.methodName || "";
    const defaultBank = checkoutQuery.data.bankAccounts.find((account) => account.active !== false)?.bankAccountId;
    const currentValues = form.getValues();

    form.reset({
      receiverName: formInitialized.current ? currentValues.receiverName : checkoutQuery.data.user.fullName || "",
      phone: formInitialized.current ? currentValues.phone : checkoutQuery.data.user.phone || "",
      address: formInitialized.current ? currentValues.address : checkoutQuery.data.user.address || "",
      notes: formInitialized.current ? currentValues.notes : "",
      paymentMethod: currentValues.paymentMethod || defaultMethod,
      bankAccountId: currentValues.bankAccountId || (defaultBank ? String(defaultBank) : ""),
    });

    formInitialized.current = true;
  }, [checkoutQuery.data, form]);

  const selectedMethod = form.watch("paymentMethod");
  const selectedBank = form.watch("bankAccountId");
  const bankTransferSelected = useMemo(
    () => selectedMethod === "BANK_TRANSFER" || selectedMethod.toUpperCase().includes("BANK"),
    [selectedMethod],
  );

  if (checkoutQuery.isLoading) {
    return <LoadingScreen label="Dang chuan bi trang thanh toan..." />;
  }

  if (checkoutQuery.isError || !checkoutQuery.data) {
    const message = getErrorMessage(checkoutQuery.error, "Khong the tai du lieu checkout. Vui long thu lai.");
    const needsProfileUpdate = message.toLowerCase().includes("so dien thoai");

    return (
      <ErrorState
        actionLabel={needsProfileUpdate ? "Cap nhat ho so" : "Quay lai gio hang"}
        message={message}
        onAction={() => navigate(needsProfileUpdate ? "/profile" : "/cart")}
      />
    );
  }

  const data = checkoutQuery.data;

  if (data.cart.items.length === 0) {
    return (
      <ErrorState
        actionLabel="Quay lai gio hang"
        message="Chua co san pham nao duoc chon de thanh toan."
        onAction={() => navigate("/cart")}
      />
    );
  }

  const applyCoupon = () => {
    const nextParams = new URLSearchParams(searchParams);
    if (couponDraft.trim()) {
      nextParams.set("couponCode", couponDraft.trim());
    } else {
      nextParams.delete("couponCode");
    }
    setSearchParams(nextParams);
  };

  return (
    <div className="split-grid">
      <section className="panel">
        <span className="eyebrow">Checkout</span>
        <h1>Hoan tat don hang cua ban</h1>
        <p className="muted-copy">
          Tong tien, dat coc va phi van chuyen deu duoc lay truc tiep tu logic hien tai cua backend.
        </p>

        {data.user.provider === "GOOGLE" && !data.user.phoneVerified ? (
          <div className="inline-alert inline-alert-warning">
            Tai khoan Google cua ban can cap nhat so dien thoai truoc khi xac nhan giao hang.
            <Link style={{ marginLeft: 8 }} to="/profile">
              Cap nhat ngay
            </Link>
          </div>
        ) : null}

        <form className="form-grid" onSubmit={form.handleSubmit((values) => placeOrder.mutate(values))}>
          <div className="field-group">
            <label htmlFor="receiverName">Nguoi nhan</label>
            <input className="field" id="receiverName" {...form.register("receiverName", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="phone">So dien thoai</label>
            <input className="field" id="phone" {...form.register("phone", { required: true })} />
          </div>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="address">Dia chi giao hang</label>
            <textarea className="textarea" id="address" rows={4} {...form.register("address", { required: true })} />
          </div>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="notes">Ghi chu</label>
            <textarea className="textarea" id="notes" rows={3} {...form.register("notes")} />
          </div>

          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label>Ma uu dai</label>
            <div className="coupon-row">
              <input
                className="field"
                onChange={(event) => setCouponDraft(event.target.value)}
                placeholder="Nhap coupon neu co"
                value={couponDraft}
              />
              <button className="button button-subtle" onClick={applyCoupon} type="button">
                Ap dung
              </button>
            </div>
          </div>

          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label>Phuong thuc thanh toan</label>
            <div className="choice-grid">
              {data.paymentMethods.map((method) => (
                <label className="choice-card" key={method.paymentMethodId}>
                  <input type="radio" value={method.methodName} {...form.register("paymentMethod", { required: true })} />
                  <div>
                    <strong>{method.methodName}</strong>
                    <p className="muted-copy">{method.description || "Thanh toan voi phuong thuc hien co cua he thong."}</p>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {bankTransferSelected ? (
            <div className="field-group" style={{ gridColumn: "1 / -1" }}>
              <label>Chon tai khoan ngan hang nhan tien</label>
              <div className="choice-grid">
                {data.bankAccounts.map((account) => (
                  <label className="choice-card" key={account.bankAccountId}>
                    <input type="radio" value={String(account.bankAccountId)} {...form.register("bankAccountId", { required: true })} />
                    <div>
                      <strong>{account.bankName}</strong>
                      <p className="muted-copy">
                        {account.accountHolder} - {account.accountNumber}
                      </p>
                      {selectedBank === String(account.bankAccountId) && account.qrImageUrl ? (
                        <img alt={account.bankName} className="bank-qr" src={account.qrImageUrl} />
                      ) : null}
                    </div>
                  </label>
                ))}
              </div>
            </div>
          ) : null}

          {placeOrder.isError ? (
            <div className="inline-alert inline-alert-danger" style={{ gridColumn: "1 / -1" }}>
              {getErrorMessage(placeOrder.error, "Khong the dat hang luc nay.")}
            </div>
          ) : null}

          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <Link className="button button-subtle" to="/cart">
              Quay lai gio hang
            </Link>
            <button className="button button-primary" disabled={placeOrder.isPending || data.cart.selectedItemCount === 0} type="submit">
              {placeOrder.isPending ? "Dang tao don..." : "Dat hang"}
            </button>
          </div>
        </form>
      </section>

      <aside className="panel">
        <span className="eyebrow">Order Summary</span>
        <h2>Don da chon</h2>
        <div className="summary-list">
          {data.cart.items.map((item) => (
            <div className="summary-product" key={item.cartItemId}>
              <div>
                <strong>{item.watch.watchName}</strong>
                <p className="muted-copy">
                  {item.watch.brandName} - x{item.quantity}
                </p>
              </div>
              <strong>{formatCurrency(item.itemTotal)}</strong>
            </div>
          ))}
          <div className="summary-row">
            <span>Tam tinh</span>
            <strong>{formatCurrency(data.summary.subtotal)}</strong>
          </div>
          <div className="summary-row">
            <span>Giam gia</span>
            <strong>{formatCurrency(data.summary.discountAmount)}</strong>
          </div>
          <div className="summary-row">
            <span>Phi van chuyen</span>
            <strong>{data.summary.shippingFee === 0 ? "Mien phi" : formatCurrency(data.summary.shippingFee)}</strong>
          </div>
          <div className="summary-row summary-row-total">
            <span>Tong cong</span>
            <strong>{formatCurrency(data.summary.totalAmount)}</strong>
          </div>
        </div>

        {data.summary.couponCode ? (
          <div className="inline-alert inline-alert-success" style={{ marginTop: 18 }}>
            Dang ap dung coupon: <strong>{data.summary.couponCode}</strong>
          </div>
        ) : null}

        {data.summary.depositRequired ? (
          <div className="inline-alert inline-alert-warning" style={{ marginTop: 18 }}>
            Don hang nay yeu cau dat coc {formatCurrency(data.summary.depositAmount)} theo rule hien tai cua he thong.
          </div>
        ) : (
          <Badge label="Khong yeu cau dat coc" tone="success" />
        )}
      </aside>
    </div>
  );
}

import { useEffect, useMemo, useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Badge } from "@/components/ui/Badge";
import { ErrorState } from "@/components/ui/ErrorState";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { getJson, postJson } from "@/lib/api/client";
import type { ApiResponse, CheckoutContext, Order } from "@/lib/api/types";
import { useI18n } from "@/lib/i18n";
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
  const { tx } = useI18n();
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
    return <LoadingScreen label={tx("Đang chuẩn bị trang thanh toán...", "Preparing checkout...")} />;
  }

  if (checkoutQuery.isError || !checkoutQuery.data) {
    const message = getErrorMessage(checkoutQuery.error, tx("Không thể tải trang thanh toán lúc này.", "We could not load checkout right now."));
    const normalizedMessage = message.toLowerCase();
    const needsProfileUpdate = normalizedMessage.includes("số điện thoại") || normalizedMessage.includes("phone");

    return (
      <ErrorState
        actionLabel={needsProfileUpdate ? tx("Cập nhật hồ sơ", "Update profile") : tx("Quay lại giỏ hàng", "Back to cart")}
        message={message}
        onAction={() => navigate(needsProfileUpdate ? "/profile" : "/cart")}
      />
    );
  }

  const data = checkoutQuery.data;

  if (data.cart.items.length === 0) {
    return (
      <ErrorState
        actionLabel={tx("Quay lại giỏ hàng", "Back to cart")}
        message={tx("Không có sản phẩm nào được chọn để thanh toán.", "No selected products are available for checkout.")}
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
        <span className="eyebrow">{tx("Thanh toán", "Checkout")}</span>
        <h1>{tx("Hoàn tất đơn hàng của bạn.", "Complete your order.")}</h1>
        <p className="muted-copy">
          {tx(
            "Tổng tiền, tiền cọc và phí vận chuyển vẫn được tính theo logic backend hiện tại.",
            "Totals, deposits and shipping remain calculated by the current backend checkout logic.",
          )}
        </p>

        {data.user.provider === "GOOGLE" && !data.user.phoneVerified ? (
          <div className="inline-alert inline-alert-warning">
            {tx(
              "Tài khoản Google của bạn vẫn cần xác minh số điện thoại trước khi xác nhận đơn hàng.",
              "Your Google account still needs a verified phone number before order confirmation.",
            )}
            <Link style={{ marginLeft: 8 }} to="/profile">
              {tx("Cập nhật hồ sơ", "Update profile")}
            </Link>
          </div>
        ) : null}

        <form className="form-grid" onSubmit={form.handleSubmit((values) => placeOrder.mutate(values))}>
          <div className="field-group">
            <label htmlFor="receiverName">{tx("Người nhận", "Receiver")}</label>
            <input className="field" id="receiverName" {...form.register("receiverName", { required: true })} />
          </div>
          <div className="field-group">
            <label htmlFor="phone">{tx("Số điện thoại", "Phone")}</label>
            <input className="field" id="phone" {...form.register("phone", { required: true })} />
          </div>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="address">{tx("Địa chỉ giao hàng", "Shipping address")}</label>
            <textarea className="textarea" id="address" rows={4} {...form.register("address", { required: true })} />
          </div>
          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label htmlFor="notes">{tx("Ghi chú", "Notes")}</label>
            <textarea className="textarea" id="notes" rows={3} {...form.register("notes")} />
          </div>

          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label>{tx("Ma giảm giá", "Coupon code")}</label>
            <div className="coupon-row">
              <input
                className="field"
                onChange={(event) => setCouponDraft(event.target.value)}
                placeholder={tx("Nhập mã giảm giá nếu bạn có", "Enter coupon if you have one")}
                value={couponDraft}
              />
              <button className="button button-subtle" onClick={applyCoupon} type="button">
                {tx("Áp dụng", "Apply")}
              </button>
            </div>
          </div>

          <div className="field-group" style={{ gridColumn: "1 / -1" }}>
            <label>{tx("Phương thức thanh toán", "Payment method")}</label>
            <div className="choice-grid">
              {data.paymentMethods.map((method) => (
                <label className="choice-card" key={method.paymentMethodId}>
                  <input type="radio" value={method.methodName} {...form.register("paymentMethod", { required: true })} />
                  <div>
                    <strong>{method.methodName}</strong>
                    <p className="muted-copy">
                      {method.description || tx("Sử dụng một trong các luồng thanh toán đang hoạt động.", "Use one of the currently active payment flows.")}
                    </p>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {bankTransferSelected ? (
            <div className="field-group" style={{ gridColumn: "1 / -1" }}>
              <label>{tx("Chọn tài khoản ngân hàng nhận tiền", "Select destination bank account")}</label>
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
              {getErrorMessage(placeOrder.error, tx("Không thể đặt hàng lúc này.", "We could not place the order right now."))}
            </div>
          ) : null}

          <div className="header-actions" style={{ gridColumn: "1 / -1", justifyContent: "space-between" }}>
            <Link className="button button-subtle" to="/cart">
              {tx("Quay lại giỏ hàng", "Back to cart")}
            </Link>
            <button className="button button-primary" disabled={placeOrder.isPending || data.cart.selectedItemCount === 0} type="submit">
              {placeOrder.isPending ? tx("Đang đặt hàng...", "Placing order...") : tx("Đặt hàng", "Place order")}
            </button>
          </div>
        </form>
      </section>

      <aside className="panel">
        <span className="eyebrow">{tx("Tóm tắt đơn hàng", "Order summary")}</span>
        <h2>{tx("Sản phẩm đã chọn", "Selected products")}</h2>
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
            <span>{tx("Tạm tính", "Subtotal")}</span>
            <strong>{formatCurrency(data.summary.subtotal)}</strong>
          </div>
          <div className="summary-row">
            <span>{tx("Giảm giá", "Discount")}</span>
            <strong>{formatCurrency(data.summary.discountAmount)}</strong>
          </div>
          <div className="summary-row">
            <span>{tx("Vận chuyển", "Shipping")}</span>
            <strong>{data.summary.shippingFee === 0 ? tx("Miễn phí", "Free") : formatCurrency(data.summary.shippingFee)}</strong>
          </div>
          <div className="summary-row summary-row-total">
            <span>{tx("Tổng cộng", "Total")}</span>
            <strong>{formatCurrency(data.summary.totalAmount)}</strong>
          </div>
        </div>

        {data.summary.couponCode ? (
          <div className="inline-alert inline-alert-success" style={{ marginTop: 18 }}>
            {tx("Đã áp dụng mã:", "Applied coupon:")} <strong>{data.summary.couponCode}</strong>
          </div>
        ) : null}

        {data.summary.depositRequired ? (
          <div className="inline-alert inline-alert-warning" style={{ marginTop: 18 }}>
            {tx(
              `Đơn hàng này cần đặt cọc ${formatCurrency(data.summary.depositAmount)} theo quy tắc thanh toán hiện tại.`,
              `This order requires a deposit of ${formatCurrency(data.summary.depositAmount)} under the current checkout rules.`,
            )}
          </div>
        ) : (
          <Badge label={tx("Không cần đặt cọc", "No deposit required")} tone="success" />
        )}
      </aside>
    </div>
  );
}

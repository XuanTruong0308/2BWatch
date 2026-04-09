export function formatCurrency(value?: number | null) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value ?? 0);
}

export function formatDate(value?: string | null) {
  if (!value) {
    return "N/A";
  }
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export function toPercent(value?: number | null) {
  return `${value ?? 0}%`;
}

export function statusTone(status?: string | null) {
  switch (status) {
    case "COMPLETED":
    case "DELIVERED":
    case "SUCCESS":
      return "success";
    case "PENDING":
    case "CONFIRMED":
      return "warning";
    case "SHIPPING":
      return "info";
    case "FAILED":
    case "CANCELLED":
      return "danger";
    default:
      return "neutral";
  }
}

export function orderStatusLabel(status?: string | null) {
  switch (status) {
    case "PENDING":
      return "Chờ xử lý";
    case "CONFIRMED":
      return "Đã xác nhận";
    case "SHIPPING":
      return "Đang giao";
    case "DELIVERED":
      return "Đã giao";
    case "COMPLETED":
      return "Hoàn thành";
    case "CANCELLED":
      return "Đã hủy";
    case "SUCCESS":
      return "Thành công";
    case "FAILED":
      return "Thất bại";
    default:
      return status ?? "Chưa xác định";
  }
}

export function getErrorMessage(error: unknown, fallback = "Yêu cầu thất bại") {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export function toBooleanText(value?: boolean | null) {
  if (value === null || value === undefined) {
    return "N/A";
  }
  return value ? "Có" : "Không";
}

import { getCurrentLanguage } from "@/lib/i18n";

export function formatCurrency(value?: number | null) {
  const language = getCurrentLanguage();
  return new Intl.NumberFormat(language === "vi" ? "vi-VN" : "en-US", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value ?? 0);
}

export function formatDate(value?: string | null) {
  const language = getCurrentLanguage();
  if (!value) {
    return language === "vi" ? "Không có" : "N/A";
  }
  return new Intl.DateTimeFormat(language === "vi" ? "vi-VN" : "en-US", {
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
  const language = getCurrentLanguage();
  switch (status) {
    case "PENDING":
      return language === "vi" ? "Chờ xử lý" : "Pending";
    case "CONFIRMED":
      return language === "vi" ? "Đã xác nhận" : "Confirmed";
    case "SHIPPING":
      return language === "vi" ? "Đang giao" : "Shipping";
    case "DELIVERED":
      return language === "vi" ? "Đã giao" : "Delivered";
    case "COMPLETED":
      return language === "vi" ? "Hoàn thành" : "Completed";
    case "CANCELLED":
      return language === "vi" ? "Đã hủy" : "Cancelled";
    case "SUCCESS":
      return language === "vi" ? "Thành công" : "Success";
    case "FAILED":
      return language === "vi" ? "Thất bại" : "Failed";
    default:
      return status ?? (language === "vi" ? "Chưa xác định" : "Unknown");
  }
}

export function getErrorMessage(error: unknown, fallback?: string) {
  const language = getCurrentLanguage();
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback ?? (language === "vi" ? "Yêu cầu thất bại" : "Request failed");
}

export function toBooleanText(value?: boolean | null) {
  const language = getCurrentLanguage();
  if (value === null || value === undefined) {
    return language === "vi" ? "Không có" : "N/A";
  }
  return value ? (language === "vi" ? "Có" : "Yes") : language === "vi" ? "Không" : "No";
}

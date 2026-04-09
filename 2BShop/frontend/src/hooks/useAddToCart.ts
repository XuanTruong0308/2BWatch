import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/hooks/useAuth";
import { postJson } from "@/lib/api/client";
import type { ApiResponse, Cart } from "@/lib/api/types";
import { getErrorMessage } from "@/lib/utils/format";

type AddToCartPayload = {
  watchId: number;
  quantity: number;
};

type AddToCartResponse = {
  cartItemCount?: number;
  cart?: Cart;
};

type AddToCartFeedback = {
  tone: "success" | "danger" | "warning";
  message: string;
} | null;

export function useAddToCart() {
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();
  const { data: user, isLoading: authLoading } = useAuth();
  const [feedback, setFeedback] = useState<AddToCartFeedback>(null);
  const [pendingWatchId, setPendingWatchId] = useState<number | null>(null);

  const mutation = useMutation({
    mutationFn: async ({ watchId, quantity }: AddToCartPayload) =>
      postJson<ApiResponse<AddToCartResponse>>("/api/v1/cart/add", { watchId, quantity }),
    onMutate: ({ watchId }) => {
      setPendingWatchId(watchId);
      setFeedback(null);
    },
    onSuccess: async (response) => {
      const payload = response.data;

      if (typeof payload?.cartItemCount === "number") {
        queryClient.setQueryData(["cart", "count"], payload.cartItemCount);
      }
      if (payload?.cart) {
        queryClient.setQueryData(["cart"], payload.cart);
      }

      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["cart", "count"] }),
        queryClient.invalidateQueries({ queryKey: ["cart"] }),
        queryClient.invalidateQueries({ queryKey: ["checkout"] }),
      ]);

      setFeedback({
        tone: "success",
        message: response.message ?? "Da them san pham vao gio hang.",
      });
    },
    onError: (error) => {
      setFeedback({
        tone: "danger",
        message: getErrorMessage(error, "Khong the them san pham vao gio hang."),
      });
    },
    onSettled: () => {
      setPendingWatchId(null);
    },
  });

  const addToCart = (watchId: number, quantity = 1) => {
    setFeedback(null);

    if (authLoading) {
      setFeedback({
        tone: "warning",
        message: "Dang kiem tra phien dang nhap. Vui long thu lai sau vai giay.",
      });
      return;
    }

    if (!user?.authenticated) {
      const continueTo = `${location.pathname}${location.search}${location.hash}`;
      navigate(`/login?continue=${encodeURIComponent(continueTo)}`);
      return;
    }

    mutation.mutate({ watchId, quantity });
  };

  return {
    addToCart,
    clearFeedback: () => setFeedback(null),
    feedback,
    isAdding: (watchId: number) => mutation.isPending && pendingWatchId === watchId,
    isPending: mutation.isPending,
  };
}

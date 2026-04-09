import { useQuery } from "@tanstack/react-query";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, CsrfPayload } from "@/lib/api/types";

export function useCsrf() {
  return useQuery({
    queryKey: ["auth", "csrf"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<CsrfPayload>>("/api/v1/auth/csrf");
      return response.data;
    },
    staleTime: 0,
    refetchOnMount: "always",
    refetchOnWindowFocus: false,
  });
}

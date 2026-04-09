import { useQuery } from "@tanstack/react-query";
import { getJson } from "@/lib/api/client";
import type { ApiResponse, AuthUser } from "@/lib/api/types";

export function useAuth() {
  return useQuery({
    queryKey: ["auth", "me"],
    queryFn: async () => {
      const response = await getJson<ApiResponse<AuthUser>>("/api/v1/auth/me");
      return response.data;
    },
  });
}

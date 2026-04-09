import { Navigate, useLocation } from "react-router-dom";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { useAuth } from "@/hooks/useAuth";

type ProtectedRouteProps = {
  children: React.ReactNode;
  requireAdmin?: boolean;
};

export function ProtectedRoute({ children, requireAdmin = false }: ProtectedRouteProps) {
  const location = useLocation();
  const { data: user, isLoading } = useAuth();

  if (isLoading) {
    return <LoadingScreen label="Đang xác thực phiên đăng nhập..." />;
  }

  if (!user?.authenticated) {
    return <Navigate to={`/login?continue=${encodeURIComponent(location.pathname + location.search)}`} replace />;
  }

  if (requireAdmin && !user.admin) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}

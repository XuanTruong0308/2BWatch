import { Navigate, useLocation } from "react-router-dom";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { useAuth } from "@/hooks/useAuth";
import { useI18n } from "@/lib/i18n";

type ProtectedRouteProps = {
  children: React.ReactNode;
  requireAdmin?: boolean;
};

export function ProtectedRoute({ children, requireAdmin = false }: ProtectedRouteProps) {
  const location = useLocation();
  const { data: user, isLoading } = useAuth();
  const { tx } = useI18n();

  if (isLoading) {
    return <LoadingScreen label={tx("Đang xác thực phiên đăng nhập...", "Verifying your sign-in session...")} />;
  }

  if (!user?.authenticated) {
    return <Navigate to={`/login?continue=${encodeURIComponent(location.pathname + location.search)}`} replace />;
  }

  if (requireAdmin && !user.admin) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}

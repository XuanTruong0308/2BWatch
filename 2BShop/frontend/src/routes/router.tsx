import { lazy } from "react";
import { Navigate, useLocation, useParams, useRoutes } from "react-router-dom";
import { AdminLayout } from "@/components/layout/AdminLayout";
import { ProtectedRoute } from "@/components/layout/ProtectedRoute";
import { PublicLayout } from "@/components/layout/PublicLayout";

const HomePage = lazy(() => import("@/features/public/pages/HomePage"));
const CatalogPage = lazy(() => import("@/features/public/pages/CatalogPage"));
const ProductDetailPage = lazy(() => import("@/features/public/pages/ProductDetailPage"));
const StaticPage = lazy(() => import("@/features/public/pages/StaticPage"));

const LoginPage = lazy(() => import("@/features/auth/pages/LoginPage"));
const RegisterPage = lazy(() => import("@/features/auth/pages/RegisterPage"));
const ConfirmRegisterPage = lazy(() => import("@/features/auth/pages/ConfirmRegisterPage"));
const ForgotPasswordPage = lazy(() => import("@/features/auth/pages/ForgotPasswordPage"));
const ResetPasswordPage = lazy(() => import("@/features/auth/pages/ResetPasswordPage"));
const PaymentResultPage = lazy(() => import("@/features/auth/pages/PaymentResultPage"));

const CartPage = lazy(() => import("@/features/user/pages/CartPage"));
const CheckoutPage = lazy(() => import("@/features/user/pages/CheckoutPage"));
const OrderConfirmationPage = lazy(() => import("@/features/user/pages/OrderConfirmationPage"));
const OrdersPage = lazy(() => import("@/features/user/pages/OrdersPage"));
const OrderDetailPage = lazy(() => import("@/features/user/pages/OrderDetailPage"));
const ProfilePage = lazy(() => import("@/features/user/pages/ProfilePage"));

const DashboardPage = lazy(() => import("@/features/admin/pages/DashboardPage"));
const BrandsPage = lazy(() => import("@/features/admin/pages/BrandsPage"));
const BrandFormPage = lazy(() => import("@/features/admin/pages/BrandFormPage"));
const WatchesPage = lazy(() => import("@/features/admin/pages/WatchesPage"));
const WatchFormPage = lazy(() => import("@/features/admin/pages/WatchFormPage"));
const UsersPage = lazy(() => import("@/features/admin/pages/UsersPage"));
const UserFormPage = lazy(() => import("@/features/admin/pages/UserFormPage"));
const UserDetailPage = lazy(() => import("@/features/admin/pages/UserDetailPage"));
const OrdersAdminPage = lazy(() => import("@/features/admin/pages/OrdersAdminPage"));
const OrderAdminDetailPage = lazy(() => import("@/features/admin/pages/OrderAdminDetailPage"));
const PaymentMethodsPage = lazy(() => import("@/features/admin/pages/PaymentMethodsPage"));
const PaymentMethodFormPage = lazy(() => import("@/features/admin/pages/PaymentMethodFormPage"));
const TransactionsPage = lazy(() => import("@/features/admin/pages/TransactionsPage"));
const TransactionDetailPage = lazy(() => import("@/features/admin/pages/TransactionDetailPage"));
const BankAccountsPage = lazy(() => import("@/features/admin/pages/BankAccountsPage"));
const BankAccountFormPage = lazy(() => import("@/features/admin/pages/BankAccountFormPage"));
const AdminSupportChatPage = lazy(() => import("@/features/admin/pages/AdminSupportChatPage"));

function ProductLegacyRedirect() {
  const { id } = useParams();
  return <Navigate replace to={id ? `/watches/${id}` : "/watches"} />;
}

function ContinueAwareRedirect() {
  const location = useLocation();
  const redirect = new URLSearchParams(location.search).get("continue");
  return <Navigate replace to={redirect || "/"} />;
}

export function RouterView() {
  return useRoutes([
    {
      element: <PublicLayout />,
      children: [
        { index: true, element: <HomePage /> },
        { path: "watches", element: <CatalogPage /> },
        { path: "watches/newest", element: <CatalogPage variant="newest" /> },
        { path: "watches/discount", element: <CatalogPage variant="discount" /> },
        { path: "watches/:id", element: <ProductDetailPage /> },
        { path: "products/:id", element: <ProductLegacyRedirect /> },
        { path: "about", element: <StaticPage /> },
        { path: "policy", element: <StaticPage /> },
        { path: "terms", element: <StaticPage /> },
        { path: "faq", element: <StaticPage /> },
        { path: "login", element: <LoginPage /> },
        { path: "register", element: <RegisterPage /> },
        { path: "confirm-register", element: <ConfirmRegisterPage /> },
        { path: "forgot-password", element: <ForgotPasswordPage /> },
        { path: "reset-password", element: <ResetPasswordPage /> },
        { path: "payment-result", element: <PaymentResultPage /> },
        {
          path: "cart",
          element: (
            <ProtectedRoute>
              <CartPage />
            </ProtectedRoute>
          ),
        },
        {
          path: "checkout",
          element: (
            <ProtectedRoute>
              <CheckoutPage />
            </ProtectedRoute>
          ),
        },
        {
          path: "checkout/confirmation/:orderId",
          element: (
            <ProtectedRoute>
              <OrderConfirmationPage />
            </ProtectedRoute>
          ),
        },
        {
          path: "profile",
          element: (
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          ),
        },
        {
          path: "my-orders",
          element: (
            <ProtectedRoute>
              <OrdersPage />
            </ProtectedRoute>
          ),
        },
        {
          path: "my-orders/:orderId",
          element: (
            <ProtectedRoute>
              <OrderDetailPage />
            </ProtectedRoute>
          ),
        },
        { path: "user/cart", element: <Navigate replace to="/cart" /> },
        { path: "user/checkout", element: <Navigate replace to="/checkout" /> },
        { path: "user/profile", element: <Navigate replace to="/profile" /> },
        { path: "user/orders", element: <Navigate replace to="/my-orders" /> },
        {
          path: "user/orders/:orderId",
          element: (
            <ProtectedRoute>
              <OrderDetailPage />
            </ProtectedRoute>
          ),
        },
        { path: "account", element: <Navigate replace to="/profile" /> },
        { path: "account/orders", element: <Navigate replace to="/my-orders" /> },
        { path: "account/change-password", element: <Navigate replace to="/profile?tab=security" /> },
        { path: "continue", element: <ContinueAwareRedirect /> },
      ],
    },
    {
      path: "admin",
      element: (
        <ProtectedRoute requireAdmin>
          <AdminLayout />
        </ProtectedRoute>
      ),
      children: [
        { index: true, element: <Navigate replace to="dashboard" /> },
        { path: "dashboard", element: <DashboardPage /> },
        { path: "brands", element: <BrandsPage /> },
        { path: "brands/new", element: <BrandFormPage /> },
        { path: "brands/:id/edit", element: <BrandFormPage /> },
        { path: "watches", element: <WatchesPage /> },
        { path: "watches/new", element: <WatchFormPage /> },
        { path: "watches/:id/edit", element: <WatchFormPage /> },
        { path: "users", element: <UsersPage /> },
        { path: "users/new", element: <UserFormPage /> },
        { path: "users/:id", element: <UserDetailPage /> },
        { path: "users/:id/edit", element: <UserFormPage /> },
        { path: "orders", element: <OrdersAdminPage /> },
        { path: "orders/:id", element: <OrderAdminDetailPage /> },
        { path: "payments/methods", element: <PaymentMethodsPage /> },
        { path: "payments/methods/new", element: <PaymentMethodFormPage /> },
        { path: "payments/methods/:id/edit", element: <PaymentMethodFormPage /> },
        { path: "payments/transactions", element: <TransactionsPage /> },
        { path: "payments/transactions/:id", element: <TransactionDetailPage /> },
        { path: "bank-accounts", element: <BankAccountsPage /> },
        { path: "bank-accounts/new", element: <BankAccountFormPage /> },
        { path: "bank-accounts/:id/edit", element: <BankAccountFormPage /> },
        { path: "support-chat", element: <AdminSupportChatPage /> },
      ],
    },
    { path: "*", element: <Navigate replace to="/" /> },
  ]);
}

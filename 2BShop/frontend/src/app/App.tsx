import { Suspense, useEffect, useState } from "react";
import { LoadingScreen } from "@/components/ui/LoadingScreen";
import { useI18n } from "@/lib/i18n";
import { RouterView } from "@/routes/router";

const DEV_APP_PORT = "5173";
const SPLASH_SESSION_KEY = "2bshop:splash-seen";

function resolveDevPortTarget() {
  if (!import.meta.env.DEV) {
    return null;
  }

  const { protocol, hostname, port, pathname, search, hash } = window.location;

  if (port === DEV_APP_PORT) {
    return null;
  }

  return `${protocol}//${hostname}:${DEV_APP_PORT}${pathname}${search}${hash}`;
}

function shouldShowInitialSplash() {
  if (typeof window === "undefined") {
    return true;
  }

  const hasSeenSplash = window.sessionStorage.getItem(SPLASH_SESSION_KEY) === "1";
  const navigationEntry = performance.getEntriesByType("navigation")[0] as PerformanceNavigationTiming | undefined;
  const navigationType = navigationEntry?.type ?? "navigate";

  if (navigationType === "reload") {
    window.sessionStorage.setItem(SPLASH_SESSION_KEY, "1");
    return true;
  }

  if (!hasSeenSplash) {
    window.sessionStorage.setItem(SPLASH_SESSION_KEY, "1");
    return true;
  }

  return false;
}

export default function App() {
  const [initialLoad, setInitialLoad] = useState(() => shouldShowInitialSplash());
  const redirectTarget = resolveDevPortTarget();
  const { tx } = useI18n();

  useEffect(() => {
    if (redirectTarget) {
      window.location.replace(redirectTarget);
      return undefined;
    }

    if (!initialLoad) {
      return undefined;
    }

    const timer = setTimeout(() => {
      setInitialLoad(false);
    }, 3400);

    return () => clearTimeout(timer);
  }, [initialLoad, redirectTarget]);

  if (redirectTarget) {
    return <LoadingScreen label={tx("Đang điều hướng đúng môi trường dev...", "Redirecting to the correct dev environment...")} />;
  }

  if (initialLoad) {
    return <LoadingScreen type="splash" />;
  }

  return (
    <Suspense fallback={<LoadingScreen label={tx("Đang tải giao diện 2BShop...", "Loading the 2BShop interface...")} />}>
      <RouterView />
    </Suspense>
  );
}

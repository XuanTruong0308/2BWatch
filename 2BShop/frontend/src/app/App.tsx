import { Suspense, useState, useEffect } from "react";
import { RouterView } from "@/routes/router";
import { LoadingScreen } from "@/components/ui/LoadingScreen";

export default function App() {
  const [initialLoad, setInitialLoad] = useState(true);

  useEffect(() => {
    // Show splash screen for exactly 4.5 seconds
    const timer = setTimeout(() => {
      setInitialLoad(false);
    }, 4500);
    return () => clearTimeout(timer);
  }, []);

  if (initialLoad) {
    return <LoadingScreen type="splash" />;
  }

  return (
    <Suspense fallback={<LoadingScreen label="Đang tải giao diện 2BShop..." />}>
      <RouterView />
    </Suspense>
  );
}

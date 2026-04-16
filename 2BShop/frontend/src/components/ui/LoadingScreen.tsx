import { useI18n } from "@/lib/i18n";

type LoadingScreenProps = {
  label?: string;
  type?: "default" | "splash";
};

export function LoadingScreen({ label, type = "default" }: LoadingScreenProps) {
  const { tx, language } = useI18n();
  const resolvedLabel = label ?? tx("Đang tải...", "Loading...");
  const splashSegments = language === "vi" ? ["2B", "SHOP", "ĐỒNG HỒ"] : ["2B", "SHOP", "WATCHES"];

  if (type === "splash") {
    return (
      <div className="splash-screen">
        <div className="splash-screen__grain" />
        <div className="splash-screen__glow" />
        <div className="splash-frame">
          <span className="splash-kicker">{tx("Trải nghiệm điện ảnh", "Cinematic loading sequence")}</span>
          <div aria-label="2BShop" className="splash-wordmark" role="img">
            {splashSegments.map((segment, index) => (
              <span
                className={`splash-segment ${index % 2 === 0 ? "splash-segment--up" : "splash-segment--down"}`}
                key={segment}
                style={{ animationDelay: `${0.18 + index * 0.16}s` }}
              >
                {segment}
              </span>
            ))}
          </div>
          <p className="splash-subtitle">
            {tx("Đồng hồ cao cấp được mở màn như một khung hình điện ảnh.", "Premium watches introduced like a cinematic frame.")}
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="screen-center">
      <div className="loading-ring" />
      <p className="muted-copy">{resolvedLabel}</p>
    </div>
  );
}

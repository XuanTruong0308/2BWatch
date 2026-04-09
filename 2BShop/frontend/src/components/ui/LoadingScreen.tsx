type LoadingScreenProps = {
  label?: string;
  type?: "default" | "splash";
};

export function LoadingScreen({ label = "Đang tải...", type = "default" }: LoadingScreenProps) {
  if (type === "splash") {
    return (
      <div className="splash-screen">
        <h1 className="splash-signature">2BShop</h1>
      </div>
    );
  }

  return (
    <div className="screen-center">
      <div className="loading-ring" />
      <p className="muted-copy">{label}</p>
    </div>
  );
}

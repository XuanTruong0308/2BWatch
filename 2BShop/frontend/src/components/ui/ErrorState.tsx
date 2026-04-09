type ErrorStateProps = {
  title?: string;
  message: string;
  actionLabel?: string;
  onAction?: () => void;
};

export function ErrorState({
  title = "Đã có lỗi xảy ra",
  message,
  actionLabel,
  onAction,
}: ErrorStateProps) {
  return (
    <div className="panel state-card">
      <h3>{title}</h3>
      <p className="muted-copy">{message}</p>
      {actionLabel && onAction ? (
        <button className="button button-primary" onClick={onAction} type="button">
          {actionLabel}
        </button>
      ) : null}
    </div>
  );
}

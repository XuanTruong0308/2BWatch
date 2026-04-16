import { useI18n } from "@/lib/i18n";

type ErrorStateProps = {
  title?: string;
  message: string;
  actionLabel?: string;
  onAction?: () => void;
};

export function ErrorState({
  title,
  message,
  actionLabel,
  onAction,
}: ErrorStateProps) {
  const { tx } = useI18n();
  const resolvedTitle = title ?? tx("Da co loi xay ra", "Something went wrong");

  return (
    <div className="panel state-card">
      <h3>{resolvedTitle}</h3>
      <p className="muted-copy">{message}</p>
      {actionLabel && onAction ? (
        <button className="button button-primary" onClick={onAction} type="button">
          {actionLabel}
        </button>
      ) : null}
    </div>
  );
}

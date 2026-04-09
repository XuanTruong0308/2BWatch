type EmptyStateProps = {
  title: string;
  description: string;
  action?: React.ReactNode;
};

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="panel state-card">
      <div className="state-ornament">
        <i className="fa-solid fa-compass" />
      </div>
      <h3>{title}</h3>
      <p className="muted-copy">{description}</p>
      {action}
    </div>
  );
}

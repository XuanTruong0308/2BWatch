import { statusTone } from "@/lib/utils/format";

type BadgeProps = {
  label: string;
  tone?: string;
};

export function Badge({ label, tone }: BadgeProps) {
  return <span className={`badge badge-${tone ?? statusTone(label)}`}>{label}</span>;
}

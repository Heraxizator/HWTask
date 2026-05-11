import { Loader2 } from 'lucide-react';

export function Loader({
  size = 24,
  className = '',
}: {
  size?: number;
  className?: string;
}) {
  return (
    <Loader2
      size={size}
      strokeWidth={2}
      className={`portal-btn__spin ${className}`.trim()}
      aria-hidden
    />
  );
}

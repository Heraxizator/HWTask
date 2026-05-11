import { TabSizes } from './enums';

type TabSize = (typeof TabSizes)[keyof typeof TabSizes];

export interface TabsProps {
  label: string;
  size?: TabSize;
  disabled?: boolean;
  isActive: boolean;
  onClick?: () => void;
  id?: string;
  className?: string;
}

/** Вкладка как в AuthTabs DO-LK: подчёркивание активной. */
export function Tabs({
  label,
  size = TabSizes.MEDIUM,
  disabled = false,
  isActive,
  onClick,
  id,
  className = '',
}: TabsProps) {
  const sizeClass = size === TabSizes.LARGE ? 'portal-tab--lg' : 'portal-tab--md';
  const activeClass = isActive ? 'portal-tab--active' : '';

  return (
    <button
      type="button"
      id={id}
      role="tab"
      aria-selected={isActive}
      disabled={disabled}
      className={`portal-tab ${sizeClass} ${activeClass} ${className}`.trim()}
      onClick={onClick}
    >
      {label}
    </button>
  );
}

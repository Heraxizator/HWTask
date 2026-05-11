import { type ReactNode } from 'react';

export interface CheckboxFieldProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label: ReactNode;
  disabled?: boolean;
  id?: string;
  className?: string;
}

export function CheckboxField({
  checked,
  onChange,
  label,
  disabled,
  id,
  className = '',
}: CheckboxFieldProps) {
  return (
    <label
      className={`portal-checkbox ${disabled ? 'portal-checkbox--disabled' : ''} ${className}`.trim()}
    >
      <input
        type="checkbox"
        id={id}
        checked={checked}
        disabled={disabled}
        onChange={(e) => onChange(e.target.checked)}
      />
      <span>{label}</span>
    </label>
  );
}

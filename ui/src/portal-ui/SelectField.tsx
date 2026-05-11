import { type ReactNode } from 'react';

import { InputSizes } from './enums';

type InputSize = (typeof InputSizes)[keyof typeof InputSizes];

function sizeClass(s: InputSize): string {
  if (s === InputSizes.SMALL) return 'portal-field__input--sm';
  if (s === InputSizes.LARGE) return 'portal-field__input--lg';
  return 'portal-field__input--md';
}

export interface SelectFieldProps {
  label?: string;
  id: string;
  name?: string;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  size?: InputSize;
  error?: boolean;
  errorText?: string;
  children: ReactNode;
  className?: string;
}

export function SelectField({
  label,
  id,
  name,
  value,
  onChange,
  disabled,
  size = InputSizes.MEDIUM,
  error = false,
  errorText,
  children,
  className = '',
}: SelectFieldProps) {
  return (
    <div className={`portal-field${error ? ' portal-field--error' : ''} ${className}`.trim()}>
      {label ? (
        <label className="portal-field__label" htmlFor={id}>
          {label}
        </label>
      ) : null}
      <select
        id={id}
        name={name}
        className={`portal-field__input portal-field__select ${sizeClass(size)}`.trim()}
        value={value}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value)}
      >
        {children}
      </select>
      {error && errorText ? <p className="portal-field__error">{errorText}</p> : null}
    </div>
  );
}

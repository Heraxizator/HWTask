import { InputSizes } from './enums';

type InputSize = (typeof InputSizes)[keyof typeof InputSizes];

function sizeClass(s: InputSize): string {
  if (s === InputSizes.SMALL) return 'portal-field__input--sm';
  if (s === InputSizes.LARGE) return 'portal-field__input--lg';
  return 'portal-field__input--md';
}

export interface TextAreaFieldProps {
  value: string;
  onChange: (value: string) => void;
  label?: string;
  id: string;
  name?: string;
  placeholder?: string;
  disabled?: boolean;
  error?: boolean;
  errorText?: string;
  size?: InputSize;
  rows?: number;
  maxLength?: number;
  required?: boolean;
  className?: string;
  textAreaClassName?: string;
}

export function TextAreaField({
  value,
  onChange,
  label,
  id,
  name,
  placeholder,
  disabled,
  error = false,
  errorText,
  size = InputSizes.MEDIUM,
  rows = 4,
  maxLength,
  required,
  className = '',
  textAreaClassName = '',
}: TextAreaFieldProps) {
  return (
    <div className={`portal-field${error ? ' portal-field--error' : ''} ${className}`.trim()}>
      {label ? (
        <label className="portal-field__label" htmlFor={id}>
          {label}
        </label>
      ) : null}
      <textarea
        id={id}
        name={name}
        className={`portal-field__input portal-field__textarea ${sizeClass(size)} ${textAreaClassName}`.trim()}
        value={value}
        rows={rows}
        maxLength={maxLength}
        required={required}
        placeholder={placeholder}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value)}
      />
      {error && errorText ? <p className="portal-field__error">{errorText}</p> : null}
    </div>
  );
}

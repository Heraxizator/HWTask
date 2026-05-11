import { type CSSProperties } from 'react';

import { InputSizes, InputTypes } from './enums';

type InputType = (typeof InputTypes)[keyof typeof InputTypes];
type InputSize = (typeof InputSizes)[keyof typeof InputSizes];

export interface InputFieldProps {
  value: string;
  onChange?: (value: string) => void;
  onInput?: (value: string) => void;
  placeholder?: string;
  label?: string;
  id?: string;
  name?: string;
  autoComplete?: string;
  disabled?: boolean;
  error?: boolean;
  errorText?: string;
  type?: InputType;
  size?: InputSize;
  className?: string;
  inputClassName?: string;
  style?: CSSProperties;
}

function inputHtmlType(t: InputType): string {
  switch (t) {
    case InputTypes.EMAIL:
      return 'email';
    case InputTypes.PASSWORD:
      return 'password';
    case InputTypes.SEARCH:
      return 'search';
    default:
      return 'text';
  }
}

function sizeClass(s: InputSize): string {
  if (s === InputSizes.SMALL) return 'portal-field__input--sm';
  if (s === InputSizes.LARGE) return 'portal-field__input--lg';
  return 'portal-field__input--md';
}

export function InputField({
  value,
  onChange,
  onInput,
  placeholder,
  label,
  id,
  name,
  autoComplete,
  disabled,
  error = false,
  errorText,
  type = InputTypes.DEFAULT,
  size = InputSizes.SMALL,
  className = '',
  inputClassName = '',
  style,
}: InputFieldProps) {
  const fieldId = id ?? name;

  return (
    <div
      className={`portal-field${error ? ' portal-field--error' : ''} ${className}`.trim()}
      style={style}
    >
      {label ? (
        <label className="portal-field__label" htmlFor={fieldId}>
          {label}
        </label>
      ) : null}
      <div className="portal-field__control-wrap">
        <input
          id={fieldId}
          name={name}
          className={`portal-field__input ${sizeClass(size)} ${inputClassName}`.trim()}
          type={inputHtmlType(type)}
          value={value}
          placeholder={placeholder}
          disabled={disabled}
          autoComplete={autoComplete}
          onChange={(e) => {
            const v = e.target.value;
            onChange?.(v);
            onInput?.(v);
          }}
        />
      </div>
      {error && errorText ? <p className="portal-field__error">{errorText}</p> : null}
    </div>
  );
}

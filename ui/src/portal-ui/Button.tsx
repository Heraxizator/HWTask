import { Loader2 } from 'lucide-react';
import {
  forwardRef,
  type ButtonHTMLAttributes,
  type ReactNode,
} from 'react';

import { ButtonColors, ButtonSizes, ButtonVariants } from './enums';

type ButtonVariant = (typeof ButtonVariants)[keyof typeof ButtonVariants];
type ButtonColor = (typeof ButtonColors)[keyof typeof ButtonColors];
type ButtonSize = (typeof ButtonSizes)[keyof typeof ButtonSizes];

const variantColorClass = (variant: ButtonVariant, color: ButtonColor): string => {
  if (variant === ButtonVariants.GHOST) return 'portal-btn--ghost';
  const colorSuffix =
    color === ButtonColors.PRIMARY
      ? 'primary'
      : color === ButtonColors.DANGER
        ? 'danger'
        : color === ButtonColors.NEUTRAL
          ? 'neutral'
          : 'secondary';
  if (variant === ButtonVariants.FILLED) return `portal-btn--filled portal-btn--${colorSuffix}`;
  if (variant === ButtonVariants.SOFT) return `portal-btn--soft portal-btn--${colorSuffix}`;
  return `portal-btn--outline portal-btn--${colorSuffix}`;
};

export interface PortalButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  color?: ButtonColor;
  size?: ButtonSize;
  loading?: boolean;
  fullWidth?: boolean;
  iconOnly?: boolean;
  children?: ReactNode;
}

export const Button = forwardRef<HTMLButtonElement, PortalButtonProps>(
  (
    {
      variant = ButtonVariants.FILLED,
      color = ButtonColors.PRIMARY,
      size = ButtonSizes.MEDIUM,
      loading = false,
      fullWidth = false,
      iconOnly = false,
      type = 'button',
      disabled,
      className = '',
      children,
      ...rest
    },
    ref,
  ) => {
    const sizeClass =
      size === ButtonSizes.SMALL ? 'portal-btn--sm' : size === ButtonSizes.LARGE ? 'portal-btn--lg' : 'portal-btn--md';
    const classes = [
      'portal-btn',
      variantColorClass(variant, variant === ButtonVariants.GHOST ? ButtonColors.NEUTRAL : color),
      sizeClass,
      iconOnly ? 'portal-btn--icon-only' : '',
      fullWidth ? 'portal-btn--full-width' : '',
      className,
    ]
      .filter(Boolean)
      .join(' ');

    return (
      <button ref={ref} type={type} className={classes} disabled={disabled || loading} {...rest}>
        {loading ? <Loader2 size={iconOnly ? 18 : 17} strokeWidth={2} className="portal-btn__spin" aria-hidden /> : null}
        {!loading || !iconOnly ? children : null}
      </button>
    );
  },
);

Button.displayName = 'Button';

/** Значения согласованы с публичным API @dvgups/ui в DO-LK (objects вместо enum — см. erasableSyntaxOnly в TS). */

export const ButtonVariants = {
  FILLED: 'FILLED',
  OUTLINE: 'OUTLINE',
  GHOST: 'GHOST',
  SOFT: 'SOFT',
} as const;

export const ButtonColors = {
  PRIMARY: 'PRIMARY',
  SECONDARY: 'SECONDARY',
  NEUTRAL: 'NEUTRAL',
  DANGER: 'DANGER',
} as const;

export const ButtonSizes = {
  SMALL: 'SMALL',
  MEDIUM: 'MEDIUM',
  LARGE: 'LARGE',
} as const;

export const InputTypes = {
  DEFAULT: 'DEFAULT',
  EMAIL: 'EMAIL',
  PASSWORD: 'PASSWORD',
  SEARCH: 'SEARCH',
} as const;

export const InputSizes = {
  SMALL: 'SMALL',
  MEDIUM: 'MEDIUM',
  LARGE: 'LARGE',
} as const;

export const TabSizes = {
  MEDIUM: 'MEDIUM',
  LARGE: 'LARGE',
} as const;

export const BadgeType = {
  FILLED: 'FILLED',
  SOFT: 'SOFT',
  COMBINE: 'COMBINE',
} as const;

export const BadgeColor = {
  PRIMARY: 'PRIMARY',
  NEUTRAL: 'NEUTRAL',
  SUCCESS: 'SUCCESS',
  WARNING: 'WARNING',
  DANGER: 'DANGER',
} as const;

export const BadgeSize = {
  SMALL: 'SMALL',
  MEDIUM: 'MEDIUM',
} as const;

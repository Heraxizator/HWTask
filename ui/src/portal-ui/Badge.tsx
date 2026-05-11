import type { ReactNode } from 'react';

import { BadgeColor, BadgeSize, BadgeType } from './enums';

type BadgeTypeVal = (typeof BadgeType)[keyof typeof BadgeType];
type BadgeColorVal = (typeof BadgeColor)[keyof typeof BadgeColor];
type BadgeSizeVal = (typeof BadgeSize)[keyof typeof BadgeSize];

function typeClass(type: BadgeTypeVal): string {
  switch (type) {
    case BadgeType.FILLED:
      return 'portal-badge--filled';
    case BadgeType.COMBINE:
      return 'portal-badge--combine';
    default:
      return 'portal-badge--soft';
  }
}

function colorClass(color: BadgeColorVal): string {
  switch (color) {
    case BadgeColor.SUCCESS:
      return 'portal-badge--success';
    case BadgeColor.WARNING:
      return 'portal-badge--warning';
    case BadgeColor.DANGER:
      return 'portal-badge--danger';
    case BadgeColor.NEUTRAL:
      return 'portal-badge--neutral';
    default:
      return 'portal-badge--primary';
  }
}

function sizeClass(size: BadgeSizeVal): string {
  return size === BadgeSize.MEDIUM ? 'portal-badge--md' : 'portal-badge--sm';
}

export interface BadgeProps {
  children?: ReactNode;
  text?: string;
  type?: BadgeTypeVal;
  color?: BadgeColorVal;
  size?: BadgeSizeVal;
  className?: string;
}

export function Badge({
  children,
  text,
  type = BadgeType.SOFT,
  color = BadgeColor.NEUTRAL,
  size = BadgeSize.SMALL,
  className = '',
}: BadgeProps) {
  const classes = ['portal-badge', typeClass(type), colorClass(color), sizeClass(size), className]
    .filter(Boolean)
    .join(' ');

  return (
    <span className={classes}>{children ?? text}</span>
  );
}

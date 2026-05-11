/**
 * Портальный UI: API и названия enums сознательно близки к @dvgups/ui в DO-LK.
 * Пакет @dvgups/ui не подключается (коммерческая разработка).
 */
export {
  BadgeColor,
  BadgeSize,
  BadgeType,
  ButtonColors,
  ButtonSizes,
  ButtonVariants,
  InputSizes,
  InputTypes,
  TabSizes,
} from './enums';
export { Badge, type BadgeProps } from './Badge';
export { Button, type PortalButtonProps } from './Button';
export { CheckboxField, type CheckboxFieldProps } from './CheckboxField';
export { InputField, type InputFieldProps } from './InputField';
export { Loader } from './Loader';
export { SelectField, type SelectFieldProps } from './SelectField';
export { Tabs, type TabsProps } from './Tabs';
export { TextAreaField, type TextAreaFieldProps } from './TextAreaField';

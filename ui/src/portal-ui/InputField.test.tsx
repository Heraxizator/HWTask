import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { InputField } from './InputField';
import { InputSizes, InputTypes } from './enums';

afterEach(() => {
  cleanup();
});

describe('portal-ui InputField', () => {
  it('renders error text when error is true', () => {
    render(
      <InputField
        id="x"
        label="Email"
        value=""
        onChange={vi.fn()}
        error
        errorText="Invalid"
        type={InputTypes.EMAIL}
      />,
    );
    expect(screen.getByText('Invalid')).toBeInTheDocument();
    const wrap = screen.getByLabelText(/Email/i).closest('.portal-field');
    expect(wrap).toHaveClass('portal-field--error');
  });

  it('calls onChange with typed value', async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(
      <InputField id="u" placeholder="login" value="" onChange={onChange} type={InputTypes.DEFAULT} size={InputSizes.SMALL} />,
    );
    await user.type(screen.getByPlaceholderText('login'), 'ab');
    expect(onChange.mock.calls.map((c) => c[0]).join('')).toBe('ab');
  });

  it('maps InputTypes.PASSWORD to password input', () => {
    render(<InputField id="p" value="" onChange={vi.fn()} type={InputTypes.PASSWORD} />);
    const input = document.getElementById('p') as HTMLInputElement;
    expect(input).toBeTruthy();
    expect(input.type).toBe('password');
  });
});

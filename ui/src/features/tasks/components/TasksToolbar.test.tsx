import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';
import { describe, expect, it, vi } from 'vitest';

import { TasksToolbar } from './TasksToolbar';

function ToolbarHarness(props: { tags: { id: string; name: string }[] }) {
  const [searchInput, setSearchInput] = useState('');
  return (
    <TasksToolbar
      searchInput={searchInput}
      onSearchChange={setSearchInput}
      tags={props.tags}
      filterTagIds={[]}
      onToggleFilterTag={vi.fn()}
    />
  );
}

describe('TasksToolbar', () => {
  it('updates search via onSearchChange when user types', async () => {
    const user = userEvent.setup();
    render(<ToolbarHarness tags={[]} />);
    const box = screen.getByRole('searchbox') as HTMLInputElement;
    await user.type(box, 'fix');
    expect(box.value).toBe('fix');
  });

  it('toggles tag filter on checkbox click', async () => {
    const user = userEvent.setup();
    const onToggle = vi.fn();
    render(
      <TasksToolbar
        searchInput=""
        onSearchChange={vi.fn()}
        tags={[{ id: 'tag-1', name: 'Urgent' }]}
        filterTagIds={[]}
        onToggleFilterTag={onToggle}
      />,
    );
    await user.click(screen.getByRole('checkbox', { name: /Urgent/i }));
    expect(onToggle).toHaveBeenCalledWith('tag-1');
  });
});

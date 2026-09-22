import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ItemAutocomplete from './ItemAutocomplete';
import { jsonResponse, mockFetch } from '../test/mockFetch';
import type { SimpleFish } from './api/FishCatch';

const aalmutter: SimpleFish = { id: 1, scientificName: "Zoarces viviparus", commonName: "Aalmutter" };

function renderAutocomplete() {
  const fetchMock = mockFetch({ "fish/search_by_common_name": () => jsonResponse([aalmutter]) });
  const onSelect = vi.fn();
  render(
    <ItemAutocomplete<SimpleFish>
      url="fish/search_by_common_name?name="
      onSelect={onSelect}
      displayFunc={(fish) => `${fish.commonName} (${fish.scientificName})`}
      setError={vi.fn()}
    />
  );
  return { fetchMock, onSelect, input: screen.getByPlaceholderText("Suchbegriff eingeben...") };
}

describe("ItemAutocomplete", () => {
  it("searches once for the debounced query", async () => {
    const { fetchMock, input } = renderAutocomplete();
    await userEvent.type(input, "aal");

    expect(await screen.findByText("Aalmutter (Zoarces viviparus)")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(fetchMock.mock.calls[0][0]).toMatch(/fish\/search_by_common_name\?name=aal$/);
  });

  it("selects an item and resets the search", async () => {
    const { onSelect, input } = renderAutocomplete();
    await userEvent.type(input, "aal");
    await userEvent.click(await screen.findByText("Aalmutter (Zoarces viviparus)"));

    expect(onSelect).toHaveBeenCalledWith(aalmutter);
    expect(input).toHaveValue("");
    expect(screen.queryByText("Aalmutter (Zoarces viviparus)")).not.toBeInTheDocument();
  });

  it("clears the results without searching for an empty query", async () => {
    const { fetchMock, input } = renderAutocomplete();
    await userEvent.type(input, "aal");
    await screen.findByText("Aalmutter (Zoarces viviparus)");

    await userEvent.clear(input);
    await vi.waitFor(() => expect(screen.queryByRole("listitem")).not.toBeInTheDocument());
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });
});

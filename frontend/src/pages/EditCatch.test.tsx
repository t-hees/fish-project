import { describe, expect, it } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import EditCatch from './EditCatch';
import { jsonResponse, mockFetch, requestBodies } from '../test/mockFetch';
import type { AllCatchesDto, SimpleFish } from '../components/api/FishCatch';

const aalmutter: SimpleFish = { id: 1, scientificName: "Zoarces viviparus", commonName: "Aalmutter" };
const oldCatches: AllCatchesDto = {
  simpleCatches: [],
  specialCatches: [
    { catchId: 7, fishId: 2, imageData: null, size: 10, weight: 20, notes: "old notes", name: "Conger conger" },
  ],
};

function renderEditCatch() {
  const fetchMock = mockFetch({
    "trip/get-catches": () => jsonResponse(oldCatches),
    "fish/search_by_common_name": () => jsonResponse([aalmutter]),
    "trip/edit-catches": () => new Response("Successfully edited catches of trip: 5"),
  });
  render(
    <MemoryRouter initialEntries={["/edit-fish?id=5"]}>
      <EditCatch />
    </MemoryRouter>
  );
  return fetchMock;
}

async function addSimpleCatch(query = "aal") {
  const [simpleSearch] = await screen.findAllByPlaceholderText("Suchbegriff eingeben...");
  await userEvent.type(simpleSearch, query);
  await userEvent.click(await screen.findByText("Aalmutter (Zoarces viviparus)"));
}

function simpleCatchRow(): HTMLElement {
  return screen.getByText("Aalmutter").closest("li")!;
}

describe("EditCatch", () => {
  it("loads the existing catches of the trip", async () => {
    const fetchMock = renderEditCatch();

    expect(await screen.findByText("Conger conger")).toBeInTheDocument();
    expect(requestBodies(fetchMock, "trip/get-catches")).toEqual([{ id: 5 }]);
  });

  it("adds simple catches once and changes their amount", async () => {
    renderEditCatch();
    await addSimpleCatch();
    // A different query, as repeating the same one within the debounce delay doesn't search again
    await addSimpleCatch("mutter");

    expect(screen.getAllByText("Aalmutter")).toHaveLength(1);
    const row = simpleCatchRow();
    expect(within(row).getByText("1")).toBeInTheDocument();

    await userEvent.click(within(row).getByText("+"));
    expect(within(row).getByText("2")).toBeInTheDocument();

    await userEvent.click(within(row).getByText("-"));
    await userEvent.click(within(row).getByText("-"));
    expect(screen.queryByText("Aalmutter")).not.toBeInTheDocument();
  });

  it("submits added and removed catches", async () => {
    const fetchMock = renderEditCatch();
    await addSimpleCatch();
    await userEvent.click(within(simpleCatchRow()).getByText("+"));
    await userEvent.click(screen.getByText("Fisch löschen"));
    expect(screen.queryByText("Conger conger")).not.toBeInTheDocument();

    await userEvent.click(screen.getByText("Absenden"));

    expect(await screen.findByText("Successfully edited catches of trip: 5")).toBeInTheDocument();
    expect(requestBodies(fetchMock, "trip/edit-catches")).toEqual([{
      tripId: 5,
      simpleCatches: [{ name: "Aalmutter", fishId: 1, amount: 2 }],
      newSpecialCatches: [],
      removableSpecialCatchIds: [7],
    }]);
    // The saved state is reloaded, so submitted new catches aren't sent again by the next submit
    expect(requestBodies(fetchMock, "trip/get-catches")).toHaveLength(2);
  });

  it("adds several detailed catches of the same fish", async () => {
    const fetchMock = renderEditCatch();
    for (const query of ["aal", "mutter"]) {
      const specialSearch = (await screen.findAllByPlaceholderText("Suchbegriff eingeben..."))[1];
      await userEvent.type(specialSearch, query);
      await userEvent.click(await screen.findByText("Aalmutter (Zoarces viviparus)"));
    }
    // Size and weight input of each catch
    const [firstSize, , secondSize] = screen.getAllByRole("spinbutton");
    await userEvent.type(firstSize, "30");
    await userEvent.type(secondSize, "45");

    await userEvent.click(screen.getByText("Absenden"));

    await screen.findByText("Successfully edited catches of trip: 5");
    const [body] = requestBodies(fetchMock, "trip/edit-catches") as { newSpecialCatches: { fishId: number, size: number }[] }[];
    expect(body.newSpecialCatches.map(({ fishId, size }) => ({ fishId, size }))).toEqual([
      { fishId: 1, size: 30 },
      { fishId: 1, size: 45 },
    ]);
  });
});

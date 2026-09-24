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

// What the backend answers to the submit of "submits added and removed catches"
const savedCatches: AllCatchesDto = {
  simpleCatches: [{ fishId: 1, amount: 2, name: "Zoarces viviparus" }],
  specialCatches: [],
};

function renderEditCatch() {
  const fetchMock = mockFetch({
    "GET trips/5/catches": () => jsonResponse(oldCatches),
    "PUT trips/5/catches": () => jsonResponse(savedCatches),
    "GET fish?name=": () => jsonResponse([aalmutter]),
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
    expect(requestBodies(fetchMock, "trips/5/catches", "GET")).toHaveLength(1);
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

    expect(await screen.findByText("Fänge gespeichert")).toBeInTheDocument();
    expect(requestBodies(fetchMock, "trips/5/catches", "PUT")).toEqual([{
      simpleCatches: [{ name: "Aalmutter", fishId: 1, amount: 2 }],
      newSpecialCatches: [],
      removableSpecialCatchIds: [7],
    }]);
    // Shows the saved catches from the response, so submitted new catches aren't sent again by the next submit
    expect(screen.getByText("Zoarces viviparus")).toBeInTheDocument();
    expect(requestBodies(fetchMock, "trips/5/catches", "GET")).toHaveLength(1);
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

    await screen.findByText("Fänge gespeichert");
    const [body] = requestBodies(fetchMock, "trips/5/catches", "PUT") as { newSpecialCatches: { fishId: number, size: number }[] }[];
    expect(body.newSpecialCatches.map(({ fishId, size }) => ({ fishId, size }))).toEqual([
      { fishId: 1, size: 30 },
      { fishId: 1, size: 45 },
    ]);
  });
});

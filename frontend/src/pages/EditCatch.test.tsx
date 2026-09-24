import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import EditCatch from './EditCatch';
import { jsonResponse, mockFetch, requestBodies } from '../test/mockFetch';
import { MAX_IMAGE_BYTES, type AllCatchesDto, type SimpleFish } from '../components/api/FishCatch';

const aalmutter: SimpleFish = { id: 1, scientificName: "Zoarces viviparus", commonName: "Aalmutter" };
const oldCatches: AllCatchesDto = {
  simpleCatches: [],
  specialCatches: [
    { catchId: 7, fishId: 2, imageUrl: "/api/trips/5/special-catches/7/image", size: 10, weight: 20,
      notes: "old notes", name: "Conger conger" },
  ],
};

// What the backend answers once the edits are saved
const savedCatches: AllCatchesDto = {
  simpleCatches: [{ fishId: 1, amount: 2, name: "Zoarces viviparus" }],
  specialCatches: [],
};

const uploadError = { code: "UNSUPPORTED_IMAGE", message: "Only JPEG and PNG images are supported",
  timestamp: "", uri: "" };

function renderEditCatch(createSpecialCatch = () => jsonResponse({}, 201)) {
  let saved = false;
  const fetchMock = mockFetch({
    "GET trips/5/catches": () => jsonResponse(saved ? savedCatches : oldCatches),
    "PUT trips/5/catches": () => { saved = true; return jsonResponse(savedCatches); },
    "POST trips/5/special-catches": createSpecialCatch,
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

// Queries differ, as repeating the same one within the debounce delay doesn't search again
async function addSpecialCatches(queries: string[]) {
  for (const query of queries) {
    const specialSearch = (await screen.findAllByPlaceholderText("Suchbegriff eingeben..."))[1];
    await userEvent.type(specialSearch, query);
    await userEvent.click(await screen.findByText("Aalmutter (Zoarces viviparus)"));
  }
}

function simpleCatchRow(): HTMLElement {
  return screen.getByText("Aalmutter").closest("li")!;
}

function fileInputs(): HTMLInputElement[] {
  return Array.from(document.querySelectorAll<HTMLInputElement>("input[type=file]"));
}

async function catchPart(form: FormData): Promise<unknown> {
  return JSON.parse(await (form.get("catch") as Blob).text());
}

describe("EditCatch", () => {
  beforeEach(() => {
    // Not implemented by jsdom
    URL.createObjectURL = vi.fn(() => "blob:preview");
    URL.revokeObjectURL = vi.fn();
  });

  it("loads the existing catches of the trip", async () => {
    const fetchMock = renderEditCatch();

    expect(await screen.findByText("Conger conger")).toBeInTheDocument();
    expect(screen.getByAltText("Foto Conger conger")).toHaveAttribute("src", oldCatches.specialCatches[0].imageUrl);
    expect(requestBodies(fetchMock, "trips/5/catches", "GET")).toHaveLength(1);
  });

  it("adds simple catches once and changes their amount", async () => {
    renderEditCatch();
    await addSimpleCatch();
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
      removableSpecialCatchIds: [7],
    }]);
    // Shows the saved catches afterwards
    expect(screen.getByText("Zoarces viviparus")).toBeInTheDocument();
    expect(requestBodies(fetchMock, "trips/5/catches", "GET")).toHaveLength(2);
  });

  it("uploads each new detailed catch with its image in its own request", async () => {
    const fetchMock = renderEditCatch();
    await addSpecialCatches(["aal", "mutter"]);
    // Size and weight input of each catch
    const [firstSize, , secondSize] = screen.getAllByRole("spinbutton");
    await userEvent.type(firstSize, "30");
    await userEvent.type(secondSize, "45");
    const photo = new File(["png data"], "photo.png", { type: "image/png" });
    await userEvent.upload(fileInputs()[0], photo);
    expect(screen.getByAltText("Foto Aalmutter")).toHaveAttribute("src", "blob:preview");

    await userEvent.click(screen.getByText("Absenden"));

    expect(await screen.findByText("Fänge gespeichert")).toBeInTheDocument();
    const [first, second] = requestBodies(fetchMock, "trips/5/special-catches", "POST") as FormData[];
    expect(await catchPart(first)).toEqual({ fishId: 1, size: 30, weight: null, notes: null });
    expect(first.get("image")).toBe(photo);
    expect(await catchPart(second)).toEqual({ fishId: 1, size: 45, weight: null, notes: null });
    expect(second.get("image")).toBeNull();
    // Saved new catches aren't shown as unsaved anymore
    expect(fileInputs()).toHaveLength(0);
  });

  it("keeps new detailed catches that couldn't be saved", async () => {
    renderEditCatch(() => jsonResponse(uploadError, 415));
    await addSpecialCatches(["aal"]);

    await userEvent.click(screen.getByText("Absenden"));

    expect(await screen.findByText(`UNSUPPORTED_IMAGE: ${uploadError.message}`)).toBeInTheDocument();
    expect(screen.queryByText("Fänge gespeichert")).not.toBeInTheDocument();
    expect(fileInputs()).toHaveLength(1);
    expect(screen.getByRole("heading", { name: "Aalmutter" })).toBeInTheDocument();
  });

  it("rejects too large images before uploading", async () => {
    renderEditCatch();
    await addSpecialCatches(["aal"]);

    const tooLarge = new File([new Uint8Array(MAX_IMAGE_BYTES + 1)], "huge.jpg", { type: "image/jpeg" });
    await userEvent.upload(fileInputs()[0], tooLarge);

    expect(screen.getByText("Das Foto ist größer als 10 MB")).toBeInTheDocument();
    expect(screen.queryByAltText("Foto Aalmutter")).not.toBeInTheDocument();
  });
});

import { useLocation } from "react-router-dom";
import { NotifiableContainer, type NotifiableContentContext } from "../components/NotifiableContainer";
import ItemAutocomplete from "../components/ItemAutocomplete";
import { useCallback, useEffect, useState } from "react";
import { Loading } from "../components/Loading";
import { fetchApi } from "../util/fetchApi";
import type { SimpleFish, SimpleCatchDto, SpecialCatchDto, SpecialCatchWithIdDto, EditCatchesDto, AllCatchesDto, NewSpecialCatch } from "../components/api/FishCatch";
import { MAX_IMAGE_BYTES, SpecialCatchList } from "../components/api/FishCatch";

export default function EditCatch() {
  return (
    <NotifiableContainer MainContent={Catch} />
  )
}

/**
 * The multipart body creating a special catch, see SpecialCatchController in the backend
 */
function toFormData(newCatch: NewSpecialCatch): FormData {
  const specialCatch: SpecialCatchDto = {
    fishId: newCatch.fishId,
    size: newCatch.size,
    weight: newCatch.weight,
    notes: newCatch.notes,
  };
  const form = new FormData();
  form.append("catch", new Blob([JSON.stringify(specialCatch)], { type: "application/json" }));
  if (newCatch.image) form.append("image", newCatch.image);
  return form;
}

function Catch({ setError, setNotification }: NotifiableContentContext) {
  const tripId: number = Number(new URLSearchParams(useLocation().search).get("id"));
  const [simpleCatches, setSimpleCatches] = useState<SimpleCatchDto[]>([]);
  const [specialCatches, setSpecialCatches] = useState<NewSpecialCatch[]>([]);
  const [oldSpecialCatches, setOldSpecialCatches] = useState<SpecialCatchWithIdDto[]>([]);
  const [removableSpecialCatchIds, setRemovableSpecialCatchIds] = useState<number[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  const catchesPath = `trips/${tripId}/catches`;

  // Replaces all local edits with the catches as currently saved
  const showSavedCatches = useCallback(async (response: Response) => {
    const savedCatches: AllCatchesDto = await response.json();
    setSimpleCatches(savedCatches.simpleCatches);
    setOldSpecialCatches(savedCatches.specialCatches);
    setSpecialCatches([]);
    setRemovableSpecialCatchIds([]);
  }, []);

  useEffect(() => {
    fetchApi(catchesPath, "GET", showSavedCatches, setError, setLoading)
  }, [catchesPath, showSavedCatches, setError]);

  if (!tripId) return (<h1>ERROR: No trip id parameter provided</h1>);
  if (loading) return <Loading />


  const updateSimpleCatches = (id: number, upFunc: (dto: SimpleCatchDto) => SimpleCatchDto) => {
    const updatedValues = simpleCatches.map(sCatch => (sCatch.fishId === id)
      ? upFunc(sCatch)
      : sCatch);
    setSimpleCatches(updatedValues);
  }

  // By position, as there can be several detailed catches of the same fish
  const updateSpecialCatches = (index: number, upFunc: (dto: NewSpecialCatch) => NewSpecialCatch) => {
    const updatedValues = specialCatches.map((sCatch, idx) => (idx === index)
      ? upFunc(sCatch)
      : sCatch);
    setSpecialCatches(updatedValues);
  }

  const selectImage = (index: number, image: File | undefined) => {
    if (image && image.size > MAX_IMAGE_BYTES) {
      setError(`Das Foto ist größer als ${MAX_IMAGE_BYTES / 1024 / 1024} MB`);
      return;
    }
    updateSpecialCatches(index, (dto) => {
      if (dto.previewUrl) URL.revokeObjectURL(dto.previewUrl);
      return {...dto, image: image ?? null, previewUrl: image ? URL.createObjectURL(image) : null};
    });
  }

  /*
  Saves the simple catches and removals first, then uploads the new special catches one by one with their image.
  New catches that couldn't be saved stay in the form, so nothing entered is lost.
  */
  const submitFish = async () => {
    let failed = false;
    const handleError = (error: string | null) => {
      failed = true;
      setError(error);
    };
    setLoading(true);
    setError(null);

    const data: EditCatchesDto = {
      simpleCatches: simpleCatches,
      removableSpecialCatchIds: removableSpecialCatchIds,
    };
    await fetchApi(catchesPath, "PUT", () => {}, handleError, () => {}, data);
    if (failed) {
      setLoading(false);
      return;
    }

    const unsavedCatches: NewSpecialCatch[] = [];
    for (const newCatch of specialCatches) {
      if (!failed) {
        await fetchApi(`trips/${tripId}/special-catches`, "POST", () => {}, handleError, () => {},
          toFormData(newCatch));
      }
      if (failed) {
        unsavedCatches.push(newCatch);
      } else if (newCatch.previewUrl) {
        URL.revokeObjectURL(newCatch.previewUrl);
      }
    }

    await fetchApi(catchesPath, "GET", showSavedCatches, handleError, setLoading);
    setSpecialCatches(unsavedCatches);
    if (unsavedCatches.length === 0) setNotification("Fänge gespeichert");
  }

  const deleteSpecialCatchButton = (fish: SpecialCatchWithIdDto) => {
    return (
      <button className="danger" onClick={() => setRemovableSpecialCatchIds([...removableSpecialCatchIds, fish.catchId])}>
        Fisch löschen
      </button>
    )
  }

  return (
    <>
      <div>
        <h2>Einfache Fischeinträge</h2>
        <ItemAutocomplete<SimpleFish>
          url="fish?name="
          onSelect={(fish: SimpleFish) => (!simpleCatches.some(scatch => scatch.fishId === fish.id))
            && setSimpleCatches([...simpleCatches, {name: fish.commonName, fishId: fish.id, amount: 1}])}
          displayFunc={(fish: SimpleFish) => `${fish.commonName} (${fish.scientificName})`}
          setError={setError}
        />
        <ul className="entry-list">
          {simpleCatches.map((fish) => (
            <li className="entry-list-item quantity-row" key={fish.fishId}>
              <span>{fish.name}</span>
              <button onClick={() => {
                if (fish.amount < 2) {
                  setSimpleCatches(simpleCatches.filter(sCatch => sCatch.fishId != fish.fishId))
                } else {
                  updateSimpleCatches(fish.fishId, (dto) => {return {...dto, amount: dto.amount - 1}})
                }
              }}>
                -
              </button>
              <span>{fish.amount}</span>
              <button onClick={() => updateSimpleCatches(fish.fishId, (dto) => {return {...dto, amount: dto.amount + 1}})}>
                +
              </button>
            </li>
          ))}
        </ul>
        <hr />
      </div>

      <div>
        <h2>Detaillierte Fischeinträge</h2>
        <SpecialCatchList
          specialCatches={oldSpecialCatches.filter(fish => !removableSpecialCatchIds.includes(fish.catchId))}
          action={deleteSpecialCatchButton}
        />
        <ItemAutocomplete<SimpleFish>
          url="fish?name="
          onSelect={(fish: SimpleFish) => setSpecialCatches([...specialCatches, {
              name: fish.commonName,
              fishId: fish.id,
              image: null,
              previewUrl: null,
              size: null,
              weight: null,
              notes: null,
          }])}
          displayFunc={(fish: SimpleFish) => `${fish.commonName} (${fish.scientificName})`}
          setError={setError}
        />
        <ul className="entry-list">
          {specialCatches.map((fish, index) => (
            // New catches are only ever appended, so their position is a stable key
            <li className="entry-list-item" key={index}>
              <h3>{fish.name}</h3>
              <label className="form-label">Foto</label>
              {fish.previewUrl &&
                <img src={fish.previewUrl} alt={`Foto ${fish.name}`}/>
              }
              <input
                type="file"
                accept="image/jpeg,image/png"
                onChange={(e) => selectImage(index, e.target.files?.[0])}
              />
              <label className="form-label">Größe</label>
              <input
                type="number"
                value={fish.size ? fish.size : ""}
                onChange={(e) => updateSpecialCatches(index, (dto) => {return {...dto, size:e.target.valueAsNumber}})}
              />
              <label className="form-label">Gewicht</label>
              <input
                type="number"
                value={fish.weight ? fish.weight : ""}
                onChange={(e) => updateSpecialCatches(index, (dto) => {return {...dto, weight:e.target.valueAsNumber}})}
              />
              <label className="form-label">Notizen</label>
              <input
                type="text"
                value={fish.notes ? fish.notes : ""}
                onChange={(e) => updateSpecialCatches(index, (dto) => {return {...dto, notes:e.target.value}})}
              />
            </li>
          ))}
        </ul>
        <hr />
      </div>

      <button className="form-submit-button" onClick={submitFish}>
        Absenden
      </button>
    </>
  )
}

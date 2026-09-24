import { useLocation } from "react-router-dom";
import { NotifiableContainer, type NotifiableContentContext } from "../components/NotifiableContainer";
import ItemAutocomplete from "../components/ItemAutocomplete";
import { useCallback, useEffect, useState } from "react";
import { Loading } from "../components/Loading";
import { fetchApi } from "../util/fetchApi";
import { encodeImage } from "../util/imageUtil";
import type { SimpleFish, SimpleCatchDto, SpecialCatchDto, SpecialCatchWithIdDto, EditCatchesDto, AllCatchesDto } from "../components/api/FishCatch";
import { SpecialCatchList } from "../components/api/FishCatch";

export default function EditCatch() {
  return (
    <NotifiableContainer MainContent={Catch} />
  )
}

function Catch({ setError, setNotification }: NotifiableContentContext) {
  const tripId: number = Number(new URLSearchParams(useLocation().search).get("id"));
  const [simpleCatches, setSimpleCatches] = useState<SimpleCatchDto[]>([]);
  const [specialCatches, setSpecialCatches] = useState<SpecialCatchDto[]>([]);
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
  const updateSpecialCatches = (index: number, upFunc: (dto: SpecialCatchDto) => SpecialCatchDto) => {
    const updatedValues = specialCatches.map((sCatch, idx) => (idx === index)
      ? upFunc(sCatch)
      : sCatch);
    setSpecialCatches(updatedValues);
  }

  const submitFish = () => {
    const data: EditCatchesDto = {
      simpleCatches: simpleCatches,
      newSpecialCatches: specialCatches,
      removableSpecialCatchIds: removableSpecialCatchIds,
    };
    setLoading(true);
    // The response holds the saved catches, the submitted new catches are among the saved ones now
    fetchApi(catchesPath, "PUT", async (response) => {
        await showSavedCatches(response);
        setNotification("Fänge gespeichert");
      },
      setError, setLoading, data)
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
              imageData: null,
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
              {fish.imageData &&
                <img src={fish.imageData} alt="Fish Foto"/>
              }
              <input
                type="file"
                accept="image/*"
                onChange={(e) => e.target.files && encodeImage(e.target.files[0], (image) => updateSpecialCatches(index,(dto) => {return {...dto, imageData: image}}))}
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

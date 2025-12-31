import { useLocation } from "react-router-dom";
import { NotifiableContainer, type NotifiableContentContext } from "../components/NotifiableContainer";
import ItemAutocomplete from "../components/ItemAutocomplete";
import { useEffect, useState } from "react";
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
  const [initialLoading, setInitialLoading] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(false);

  const initializeOldCatches = (oldCatches: AllCatchesDto) => {
    console.log(oldCatches);
    setSimpleCatches(oldCatches.simpleCatches)
    setOldSpecialCatches(oldCatches.specialCatches)
  }

  useEffect(() => {
    fetchApi("trip/get-catches", "POST", async (response) => initializeOldCatches(await response.json()),
      setError, setInitialLoading, {id: tripId})
  }, [setError, tripId]);

  if (!tripId) return (<h1>ERROR: No trip id parameter provided</h1>);
  if (initialLoading) return <Loading />


  const updateSimpleCatches = (id: number, upFunc: (dto: SimpleCatchDto) => SimpleCatchDto) => {
    const updatedValues = simpleCatches.map(sCatch => (sCatch.fishId === id)
      ? upFunc(sCatch)
      : sCatch);
    setSimpleCatches(updatedValues);
  }

  const updateSpecialCatches = (id: number, upFunc: (dto: SpecialCatchDto) => SpecialCatchDto) => {
    const updatedValues = specialCatches.map(sCatch => (sCatch.fishId === id)
      ? upFunc(sCatch)
      : sCatch);
    setSpecialCatches(updatedValues);
  }

  const submitFish = () => {
    const data: EditCatchesDto = {
      tripId: tripId,
      simpleCatches: simpleCatches,
      newSpecialCatches: specialCatches,
      removableSpecialCatchIds: removableSpecialCatchIds,
    };
    fetchApi("trip/edit-catches", "POST", async (response) => console.log(await response.text()),
      setError, setLoading, data)
  }

  const deleteSpecialCatchButton = (fish: SpecialCatchWithIdDto) => {
    return (
      <button onClick={() => setRemovableSpecialCatchIds([...removableSpecialCatchIds, fish.catchId])}>
        Fish Löschen
      </button>
    )
  }

  return (
    <>
      <FishList setError={setError} setNotification={setNotification} />

      <div>
        <h2>Einfache Fischeinträge</h2>
        <ItemAutocomplete<SimpleFish>
          url="fish/search_by_common_name?name="
          onSelect={(fish: SimpleFish) => (!simpleCatches.some(scatch => scatch.fishId === fish.id))
            && setSimpleCatches([...simpleCatches, {name: fish.commonName, fishId: fish.id, amount: 1}])}
          displayFunc={(fish: SimpleFish) => `${fish.commonName} (${fish.scientificName})`}
          setError={setError}
        />
        <ul>
          {simpleCatches.map((fish) => (
            <li key={fish.fishId}>
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
          url="fish/search_by_common_name?name="
          onSelect={(fish: SimpleFish) => (!specialCatches.some(scatch => scatch.fishId === fish.id))
            && setSpecialCatches([...specialCatches, {
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
        <ul>
          {specialCatches.map((fish) => (
            <li key={fish.fishId}>
              <h2>{fish.name}</h2>
              <label className="form-label">Foto</label>
              {fish.imageData &&
                <img src={fish.imageData} alt="Fish Foto"/>
              }
              <input
                type="file"
                accept="image/*"
                onChange={(e) => e.target.files && encodeImage(e.target.files[0], (image) => updateSpecialCatches(fish.fishId, (dto) => {return {...dto, imageData: image}}))}
              />
              <label className="form-label">Größe</label>
              <input
                type="number"
                value={fish.size ? fish.size : ""}
                onChange={(e) => updateSpecialCatches(fish.fishId, (dto) => {return {...dto, size: e.target.valueAsNumber}})}
              />
              <label className="form-label">Gewicht</label>
              <input
                type="number"
                value={fish.weight ? fish.weight : ""}
                onChange={(e) => updateSpecialCatches(fish.fishId, (dto) => {return {...dto, weight: e.target.valueAsNumber}})}
              />
              <label className="form-label">Notizen</label>
              <input
                type="text"
                value={fish.notes ? fish.notes : ""}
                onChange={(e) => updateSpecialCatches(fish.fishId, (dto) => {return {...dto, notes: e.target.value}})}
              />
            </li>
          ))}
        </ul>
        <hr />
      </div>

      <button onClick={submitFish}>
        Absenden
      </button>
    </>
  )
}

function FishList({ setError }: NotifiableContentContext) {
  return (<></>)
}

import type { JSX } from "react";

export type SimpleFish = {
  id: number,
  scientificName: string;
  commonName: string;
};

export type SimpleCatchDto = {
  fishId: number,
  amount: number,
  name: string|null,
}

// Sent as the "catch" part when creating a special catch, its image is a separate part
export type SpecialCatchDto = {
  fishId: number,
  size: number|null,
  weight: number|null,
  notes: string|null,
}

// A special catch that isn't saved yet
export type NewSpecialCatch = SpecialCatchDto & {
  name: string,
  image: File|null,
  // Object URL of image for showing it before the upload
  previewUrl: string|null,
}

export type SpecialCatchWithIdDto = {
  catchId: number,
  fishId: number,
  imageUrl: string|null,
  size: number|null,
  weight: number|null,
  notes: string|null,
  name: string|null,
}

export type EditCatchesDto = {
  simpleCatches: SimpleCatchDto[],
  removableSpecialCatchIds: number[],
}

// Checked by the backend as well, this only saves uploading too large images
export const MAX_IMAGE_BYTES = 10 * 1024 * 1024;

export type AllCatchesDto = {
  simpleCatches: SimpleCatchDto[],
  specialCatches: SpecialCatchWithIdDto[]
}

export type SpecialCatchListType = {
  specialCatches: SpecialCatchWithIdDto[],
  action?: (fish: SpecialCatchWithIdDto) => JSX.Element
};
export function SpecialCatchList({ specialCatches, action }: SpecialCatchListType) {
  return(
    <div className="entry-list">
      {specialCatches.map((fish) => {
        return <div className="entry-list-item" key={fish.catchId}>
                 <h3>{fish.name}</h3>
                 {fish.imageUrl &&
                   <a href={fish.imageUrl} target="_blank" rel="noreferrer">
                     <img src={fish.imageUrl} alt={`Foto ${fish.name}`} loading="lazy"/>
                   </a>
                 }
                 <p>Größe: {fish.size}</p>
                 <p>Gewicht: {fish.weight}</p>
                 <p>Notizen: {fish.notes}</p>
                 {action && action(fish)}
               </div>
      })}
    </div>
  )
}

export function SimpleCatchList({ simpleCatches }: {simpleCatches: SimpleCatchDto[]}) {
  return(
    <table className="simple-catch-table">
      <tbody>
        {simpleCatches.map((fish) => {
          return <tr key={fish.fishId}>
                   <td>{fish.name}</td>
                   <td>{fish.amount}</td>
                 </tr>
        })}
      </tbody>
    </table>
  )
}

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

export type SpecialCatchDto = {
  fishId: number,
  imageData: string|null,
  size: number|null,
  weight: number|null,
  notes: string|null,
  name: string|null,
}

export type SpecialCatchWithIdDto = {
  catchId: number,
  fishId: number,
  imageData: string|null,
  size: number|null,
  weight: number|null,
  notes: string|null,
  name: string|null,
}

export type EditCatchesDto = {
  tripId: number,
  simpleCatches: SimpleCatchDto[],
  newSpecialCatches: SpecialCatchDto[],
  removableSpecialCatchIds: number[],
}

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
                 {fish.imageData && <img src={fish.imageData} alt="noimage"/>}
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

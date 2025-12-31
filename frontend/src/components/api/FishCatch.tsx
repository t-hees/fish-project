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
    <div>
      {specialCatches.map((fish) => {
        return <div>
                 <h3>Name: {fish.name}</h3>
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
    <table>
      {simpleCatches.map((fish) => {
        return <tr>
                 <td>{fish.name}</td>
                 <td>{fish.amount}</td>
               </tr>
      })}
    </table>
  )
}

import { useState } from "react";
import { NotifiableContainer, type NotifiableContentContext } from "../components/NotifiableContainer";
import { fetchApi } from "../util/fetchApi";
import { useNavigate } from "react-router-dom";
import { Loading } from "../components/Loading";

export default function CreateTrip() {
  return (
    <NotifiableContainer MainContent={Trip}/>
  );
}

const environmentList = ["RIVER", "LAKE", "OCEAN"] as const;
const weatherList = [
        "CLEAR_SKY",
        "PARTLY_CLOUDY",
        "CLOUDY",
        "OVERCAST",
        "FOGGY",
        "HUMID",
        "THUNDERSTORM",
        "LIGHT_RAIN",
        "HEAVY_RAIN",
        "LIGHT_WIND",
        "STRONG_WIND",
        "STORM",
        "FREEZING",
        "SNOW",
        "DUSK",
        "DAWN",
];

export type TripDto = {
  location: string,
  environment: typeof environmentList[number],
  time: number,
  hours: number,
  temperature: number,
  waterLevel: number,
  weather: string[],
  notes: string,
}

function Trip({ setError }: NotifiableContentContext) {
  const navigate = useNavigate();
  const [loading, setLoading] = useState<boolean>(false);
  const [trip, setTripDto] = useState<TripDto>({} as TripDto);

  const handleResponse = async (response: Response) => {
    const message = await response.text();
    console.log(message);
    navigate("/home");
  }

  const handleError = (err: string|null) => {
    setLoading(false);
    setError(err);
  }

  const tripFromInput = (labelText: string, dtoElement: keyof TripDto,
    labelType: "text"|"number"|"datetime-local", isRequired?: boolean) => {

    return(
      <>
        <label className="form-label">{labelText}: </label>
        <input
          type={labelType}
          value={trip[dtoElement]}
          onChange={(e) => setTripDto({...trip, [dtoElement]: e.target.value})}
          required={isRequired ? true : false}
        />
      </>
    )
  }

  const tripFormSelection = (labelText: string, dtoElement: keyof TripDto,
    selectionList: readonly string[] | readonly number[], isMultiSelection?: boolean) => {

    const singleSelection = (
      <select
        value={trip[dtoElement]}
        onChange={(e) => setTripDto({...trip, [dtoElement]: e.target.value})}
      >
        <option value="">----</option>
        {selectionList.map((element, idx) => (
          <option key={idx} value={element}>
            {element}
          </option>
        ))}
      </select>
    )

    const multiSelection = (
      <select
        value={trip[dtoElement]}
        multiple
        onChange={(e) => setTripDto({...trip, [dtoElement]: Array.from(e.target.selectedOptions, (option) => option.value)})}
      >
        <option value="">----</option>
        {selectionList.map((element, idx) => (
          <option key={idx} value={element}>
            {element}
          </option>
        ))}
      </select>
    )
    return(
      <>
        <label className="form-label">{labelText}: </label>
        {isMultiSelection ? multiSelection : singleSelection}
      </>
    )
  }

  return (
    <>
      <h2> Neuer Angelausflug </h2>
      {loading && <Loading />}
      <form onSubmit={(e) => {
        e.preventDefault();
        fetchApi("trip/create", "POST", handleResponse, handleError, setLoading, trip)
      }}>
        {tripFromInput("Tag/Uhrzeit", "time", "datetime-local", true)}
        {tripFromInput("Ort", "location", "text", true)}
        {tripFormSelection("Gewässerart", "environment", environmentList)}
        {tripFromInput("Dauer(in h)", "hours", "number")}
        {tripFromInput("Temperatur", "temperature", "number")}
        {tripFromInput("Wasserpegel", "waterLevel", "number")}
        {tripFormSelection("Wetter", "weather", weatherList, true)}
        {tripFromInput("Notizen", "notes", "text")}
        <div>
          <button className="form-submit-button" type="submit">Angelausflug erstellen</button>
        </div>
      </form>
    </>
  );
}

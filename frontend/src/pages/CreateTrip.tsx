import { useState } from "react";
import { NotifiableContainer, type NotifiableContentContext } from "../components/NotifiableContainer";
import { fetchApi } from "../util/fetchApi";
import { useNavigate } from "react-router-dom";
import { Loading } from "../components/Loading";
import { environmentList, weatherList, type TripDto } from "../components/api/Trip";
import { useTranslation } from "../i18n/LanguageContext";

export default function CreateTrip() {
  return (
    <NotifiableContainer MainContent={Trip}/>
  );
}

function Trip({ setError }: NotifiableContentContext) {
  const navigate = useNavigate();
  const [loading, setLoading] = useState<boolean>(false);
  const [trip, setTripDto] = useState<TripDto>({} as TripDto);
  const { t } = useTranslation();

  const handleResponse = async () => {
    navigate("/");
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
      <h2> {t.trip.newTrip} </h2>
      {loading && <Loading />}
      <form onSubmit={(e) => {
        e.preventDefault();
        fetchApi("trips", "POST", handleResponse, handleError, setLoading, trip)
      }}>
        {tripFromInput(t.trip.dateTime, "time", "datetime-local", true)}
        {tripFromInput(t.trip.location, "location", "text", true)}
        {tripFormSelection(t.trip.environment, "environment", environmentList)}
        {tripFromInput(t.trip.durationInHours, "hours", "number")}
        {tripFromInput(t.trip.temperature, "temperature", "number")}
        {tripFromInput(t.trip.waterLevel, "waterLevel", "number")}
        {tripFormSelection(t.trip.weather, "weather", weatherList, true)}
        {tripFromInput(t.trip.notes, "notes", "text")}
        <div>
          <button className="form-submit-button" type="submit">{t.trip.createTrip}</button>
        </div>
      </form>
    </>
  );
}

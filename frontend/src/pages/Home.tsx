import { useEffect, useState } from "react";
import { NotifiableContainer, type NotifiableContentContext, type WrappedComponent } from "../components/NotifiableContainer";
import { fetchApi } from "../util/fetchApi";
import { Loading } from "../components/Loading";
import { useNavigate } from "react-router-dom";

type Trip = undefined;

export default function Home() {
  const OuterWrapper = ({ InnerComponent }: WrappedComponent) => {
    return (
    <div className="full-page-container">
      <div className="search-bar">
        <TripSearchBar />
      </div>
      <InnerComponent />
    </div>
    );
  }

  return (
    <div className="full-page-container">
      <NotifiableContainer MainContent={TripContainer} ContentWrapper={OuterWrapper} />
    </div>
  );
}

function TripContainer ({ setError }: NotifiableContentContext) {
  const navigate = useNavigate();
  const [tripList, setTripList] = useState<Trip[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    const relPath = "trip/all";
    fetchApi(relPath, "GET", async (response) => setTripList(await response.json()),
      setError, setLoading)
  }, [])

  tripList.map((trip) => console.log(trip));

  return (
    <div className="scroll-container">
      {loading && <Loading />}
      <button type="button" onClick={() => navigate("/create-trip")}>
        CREATE TRIP
      </button>
      {tripList.map((trip) =>
        <div className="trip-container">
          {trip.id}
        </div>
      )}
    </div>
  );
}

function CreateTrip() {
}

function TripSearchBar() {
  return (
    <>
      Lorem Ipsum
    </>
  );
}

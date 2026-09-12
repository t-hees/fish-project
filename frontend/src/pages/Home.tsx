import { useCallback, useEffect, useRef, useState } from "react";
import { NotifiableContainer, type NotifiableContentContext, type WrappedComponent } from "../components/NotifiableContainer";
import { fetchApi } from "../util/fetchApi";
import { Loading } from "../components/Loading";
import { useNavigate } from "react-router-dom";
import "./Home.css";
import type { TripDto } from "../components/api/Trip";
import { SimpleCatchList, SpecialCatchList, type AllCatchesDto, type SimpleCatchDto, type SpecialCatchWithIdDto } from "../components/api/FishCatch";

type Trip = TripDto & {
  id: number,
}

type TripPageDto = {
  trips: Trip[],
  hasNext: boolean,
  totalElements: number,
}

const TRIP_PAGE_SIZE = 10;

export default function Home() {
  const OuterWrapper = ({ InnerComponent }: WrappedComponent) => {
    return (
    <div className="main-flex-container full-page">
      <div className="search-bar">
        <TripSearchBar />
      </div>
      <InnerComponent />
    </div>
    );
  }

  return (
    <NotifiableContainer MainContent={TripContainer} ContentWrapper={OuterWrapper} />
  );
}

function TripContainer ({ setError, setNotification }: NotifiableContentContext) {
  const navigate = useNavigate();
  const [tripList, setTripList] = useState<Trip[]>([]);
  const [page, setPage] = useState<number>(0);
  const [hasNext, setHasNext] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(false);
  const [expandedTrips, setExpandedTrips] = useState<Set<number>>(new Set());
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const loadingRef = useRef<boolean>(false); // necessary to avoid reloading same page

  const loadNextPage = useCallback(() => {
    if (loadingRef.current || !hasNext) return;
    loadingRef.current = true;
    setLoading(true);
    fetchApi(`trip/all?page=${page}&size=${TRIP_PAGE_SIZE}`, "GET",
      async (response) => {
        const tripPage: TripPageDto = await response.json();
        setTripList((prev) => [...prev, ...tripPage.trips]);
        setHasNext(tripPage.hasNext);
        setPage((prev) => prev + 1);
      },
      setError,
      (isLoading) => {
        loadingRef.current = isLoading;
        setLoading(isLoading);
      }
    )
  }, [page, hasNext, setError])

  useEffect(() => {
    loadNextPage();
    // Only meant to run once on mount, loadNextPage advances its own page/hasNext state
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) {
        loadNextPage();
      }
    });
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [loadNextPage])

  const toggleTripContainer = (id: number) => {
    const newSet = new Set(expandedTrips);
    if (expandedTrips.has(id)) {
      newSet.delete(id);
    } else {
      newSet.add(id);
    }
    setExpandedTrips(newSet);
  }

  const tripDelete = (tripId: number) => {
    fetchApi("trip/delete", "POST", async (response) => setNotification(await response.text()),
      setError, setLoading, {id: tripId})
  }

  return (
    <div className="scroll-container">
      {loading && <Loading />}
      <button type="button" onClick={() => navigate("/create-trip")}>
        Neuer Angelausflug
      </button>
      {tripList.map((trip) =>
        <div className="trip-container" onClick={() => toggleTripContainer(trip.id)}>
          <h2>{trip.time.toString().split("T")[0]} - {trip.location}</h2>
          <div className={`trip-container-body ${expandedTrips.has(trip.id) ? "expanded" : "collapsed"}`}>
            {/*
            <button type="button" onClick={() => 0}>
              Angelausflug bearbeiten
            </button>
              */}
            <div>
              <p>Uhrzeit: {trip.time.toString().split("T")[1]}</p>
              <p>Gewässerart: {trip.environment}</p>
              <p>Dauer: {trip.hours && trip.hours + " Stunden"}</p>
              <p>Temperatur: {trip.temperature}</p>
              <p>Wasserpegel: {trip.waterLevel}</p>
              <p>Wetter: {trip.weather && trip.weather.join(", ")}</p>
              <p>Notizen:</p>
              {trip.notes}
            </div>
            <button type="button" onClick={() => navigate(`/edit-fish?id=${trip.id}`)}>
              Fishliste bearbeiten
            </button>
            {expandedTrips.has(trip.id) && <TripCatches tripId={trip.id} setError={setError}/>}
            <button type="button" onClick={() => tripDelete(trip.id)}>
              Eintrag Löschen
            </button>
          </div>
        </div>
      )}
      {hasNext && <div ref={sentinelRef} />}
    </div>
  );
}

function TripCatches({ tripId, setError }: {tripId: number, setError: React.Dispatch<string|null>}) {
  const [simpleCatches, setSimpleCatches] = useState<SimpleCatchDto[]>([]);
  const [specialCatches, setSpecialCatches] = useState<SpecialCatchWithIdDto[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  const initializeCatches = (oldCatches: AllCatchesDto) => {
    console.log(oldCatches);
    setSimpleCatches(oldCatches.simpleCatches)
    setSpecialCatches(oldCatches.specialCatches)
  }
  useEffect(() => {
    fetchApi("trip/get-catches", "POST", async (response) => initializeCatches(await response.json()),
      setError, setLoading, {id: tripId})
  }, []);

  if (loading) return <Loading />

  return (
    <div>
      <hr />
      <SimpleCatchList simpleCatches={simpleCatches} />
      <hr />
      <SpecialCatchList specialCatches={specialCatches} />
      <hr />
    </div>
  )
}

function TripSearchBar() {
  return (
    <>
      Lorem Ipsum
    </>
  );
}

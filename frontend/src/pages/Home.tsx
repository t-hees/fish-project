import { useCallback, useEffect, useRef, useState } from "react";
import { NotifiableContainer, type NotifiableContentContext, type WrappedComponent } from "../components/NotifiableContainer";
import { fetchApi } from "../util/fetchApi";
import { Loading } from "../components/Loading";
import { useNavigate } from "react-router-dom";
import { useDebounce } from "../util/useDebounce";
import "./Home.css";
import { environmentList, type TripDto } from "../components/api/Trip";
import { buildTripQuery, EMPTY_SEARCH_PARAMS, type TripSearchParams } from "../util/tripQuery";
import { SimpleCatchList, SpecialCatchList, type AllCatchesDto, type SimpleCatchDto, type SpecialCatchWithIdDto } from "../components/api/FishCatch";
import { useTranslation } from "../i18n/LanguageContext";

type Trip = TripDto & {
  id: number,
}

type TripPageDto = {
  trips: Trip[],
  hasNext: boolean,
  totalElements: number,
}

const TRIP_PAGE_SIZE = 10;
const SEARCH_DEBOUNCE_MS = 400;

export default function Home() {
  const [searchParams, setSearchParams] = useState<TripSearchParams>(EMPTY_SEARCH_PARAMS);

  // Stable across renders so the search inputs (and their debounce timer) aren't remounted on every keystroke
  const OuterWrapper = useCallback(({ children }: WrappedComponent) => {
    return (
    <div className="main-flex-container full-page">
      <div className="search-bar">
        <TripSearchBar onSearch={setSearchParams} />
      </div>
      {children}
    </div>
    );
  }, []);

  // Recreated whenever the debounced search params change, which remounts TripContainer
  // and thereby resets pagination and triggers a fresh page-0 fetch with the new filters
  const MainContent = useCallback((props: NotifiableContentContext) => (
    <TripContainer {...props} searchParams={searchParams} />
  ), [searchParams]);

  return (
    <NotifiableContainer MainContent={MainContent} ContentWrapper={OuterWrapper} />
  );
}

function TripContainer ({ setError, setNotification, searchParams }: NotifiableContentContext & { searchParams: TripSearchParams }) {
  const navigate = useNavigate();
  const [tripList, setTripList] = useState<Trip[]>([]);
  const [hasNext, setHasNext] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(false);
  const [expandedTrips, setExpandedTrips] = useState<Set<number>>(new Set());
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const loadingRef = useRef<boolean>(false); // necessary to avoid reloading same page
  // Refs instead of state, as the observer may call loadNextPage after a page loaded but before the
  // rerender replaced its closure, which would otherwise load (and append) the same page again
  const pageRef = useRef<number>(0);
  const hasNextRef = useRef<boolean>(true);
  const { t } = useTranslation();

  const loadNextPage = useCallback(() => {
    if (loadingRef.current || !hasNextRef.current) return;
    loadingRef.current = true;
    setLoading(true);
    fetchApi(`trips?${buildTripQuery(pageRef.current, TRIP_PAGE_SIZE, searchParams)}`, "GET",
      async (response) => {
        const tripPage: TripPageDto = await response.json();
        pageRef.current += 1;
        hasNextRef.current = tripPage.hasNext;
        setTripList((prev) => [...prev, ...tripPage.trips]);
        setHasNext(tripPage.hasNext);
      },
      setError,
      (isLoading) => {
        loadingRef.current = isLoading;
        setLoading(isLoading);
      }
    )
  }, [searchParams, setError])

  useEffect(() => {
    loadNextPage();
    // Only meant to run once on mount, loadNextPage advances its own page/hasNext refs
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
    fetchApi(`trips/${tripId}`, "DELETE", () => {
        setTripList((prev) => prev.filter((trip) => trip.id !== tripId));
        setNotification(t.trip.tripDeleted);
      },
      setError, setLoading)
  }

  return (
    <div className="scroll-container">
      {loading && <Loading />}
      <button type="button" onClick={() => navigate("/create-trip")}>
        {t.trip.newTrip}
      </button>
      {tripList.map((trip) => {
        const isExpanded = expandedTrips.has(trip.id);
        return (
          <div className="trip-card" key={trip.id}>
            <button
              type="button"
              className="trip-card-header"
              onClick={() => toggleTripContainer(trip.id)}
              aria-expanded={isExpanded}
            >
              <span className="trip-card-heading">
                <span className="trip-date">{trip.time.toString().split("T")[0]}</span>
                <span className="trip-location">{trip.location}</span>
              </span>
              <span className={`trip-chevron ${isExpanded ? "expanded" : ""}`}>▾</span>
            </button>
            <div className={`trip-card-body ${isExpanded ? "expanded" : ""}`}>
              <div className="trip-card-body-inner">
                <div className="trip-details">
                  <div className="trip-detail"><span className="label">{t.trip.time}</span><span>{trip.time.toString().split("T")[1]}</span></div>
                  <div className="trip-detail"><span className="label">{t.trip.environment}</span><span>{trip.environment}</span></div>
                  <div className="trip-detail"><span className="label">{t.trip.duration}</span><span>{trip.hours ? t.trip.hours(trip.hours) : "-"}</span></div>
                  <div className="trip-detail"><span className="label">{t.trip.temperature}</span><span>{trip.temperature}</span></div>
                  <div className="trip-detail"><span className="label">{t.trip.waterLevel}</span><span>{trip.waterLevel}</span></div>
                  <div className="trip-detail"><span className="label">{t.trip.weather}</span><span>{trip.weather && trip.weather.length > 0 ? trip.weather.join(", ") : "-"}</span></div>
                </div>
                {trip.notes && (
                  <div className="trip-notes">
                    <span className="label">{t.trip.notes}</span>
                    <p>{trip.notes}</p>
                  </div>
                )}
                {isExpanded && <TripCatches tripId={trip.id} setError={setError}/>}
                <div className="trip-card-actions">
                  <button type="button" onClick={() => navigate(`/edit-fish?id=${trip.id}`)}>
                    {t.trip.editCatches}
                  </button>
                  <button type="button" className="danger" onClick={() => tripDelete(trip.id)}>
                    {t.trip.deleteTrip}
                  </button>
                </div>
              </div>
            </div>
          </div>
        );
      })}
      {hasNext && <div ref={sentinelRef} />}
    </div>
  );
}

function TripCatches({ tripId, setError }: {tripId: number, setError: React.Dispatch<string|null>}) {
  const [simpleCatches, setSimpleCatches] = useState<SimpleCatchDto[]>([]);
  const [specialCatches, setSpecialCatches] = useState<SpecialCatchWithIdDto[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const { t } = useTranslation();

  useEffect(() => {
    fetchApi(`trips/${tripId}/catches`, "GET", async (response) => {
        const catches: AllCatchesDto = await response.json();
        setSimpleCatches(catches.simpleCatches);
        setSpecialCatches(catches.specialCatches);
      },
      setError, setLoading)
  }, [setError, tripId]);

  if (loading) return <Loading />

  const isEmpty = simpleCatches.length === 0 && specialCatches.length === 0;

  return (
    <div className="trip-catches">
      <span className="label section-title">{t.trip.catches}</span>
      {isEmpty
        ? <p className="empty-state">{t.trip.noCatches}</p>
        : <>
            {simpleCatches.length > 0 && <SimpleCatchList simpleCatches={simpleCatches} />}
            {specialCatches.length > 0 && <SpecialCatchList specialCatches={specialCatches} />}
          </>
      }
    </div>
  )
}

function TripSearchBar({ onSearch }: { onSearch: (searchParams: TripSearchParams) => void }) {
  const [rawParams, setRawParams] = useState<TripSearchParams>(EMPTY_SEARCH_PARAMS);
  const debouncedParams = useDebounce<TripSearchParams>(rawParams, SEARCH_DEBOUNCE_MS);
  const { t } = useTranslation();

  useEffect(() => {
    onSearch(debouncedParams);
    // onSearch is the setSearchParams setter from Home, stable across renders
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedParams])

  return (
    <div className="trip-search-bar">
      <input
        type="text"
        placeholder={t.trip.searchLocation}
        value={rawParams.location}
        onChange={(e) => setRawParams({ ...rawParams, location: e.target.value })}
        className="search-input"
      />
      <select
        value={rawParams.environment}
        onChange={(e) => setRawParams({ ...rawParams, environment: e.target.value as TripSearchParams["environment"] })}
      >
        <option value="">{t.trip.allEnvironments}</option>
        {environmentList.map((environment) => (
          <option key={environment} value={environment}>{environment}</option>
        ))}
      </select>
      <label className="form-label">{t.trip.from}</label>
      <input
        type="date"
        value={rawParams.from}
        onChange={(e) => setRawParams({ ...rawParams, from: e.target.value })}
      />
      <label className="form-label">{t.trip.to}</label>
      <input
        type="date"
        value={rawParams.to}
        onChange={(e) => setRawParams({ ...rawParams, to: e.target.value })}
      />
    </div>
  );
}

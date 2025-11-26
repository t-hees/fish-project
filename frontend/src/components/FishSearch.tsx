import { useEffect, useState, type ChangeEvent } from "react";
import { useDebounce } from "../util/useDebounce";
import { Loading } from "./Loading";
import { fetchApi } from "../util/fetchApi";

type SimpleFish = {
  scientificName: string;
  commonName: string;
};

export default function FishSearch() {
  const [fishList, setFishList]  = useState<SimpleFish[]>([]);
  const [query, setQuery] = useState<string>("");
  const debouncedQuery = useDebounce<string>(query, 500);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    fetchFish(debouncedQuery);
  }, [debouncedQuery])

  const fetchFish = (query: string) => {
    const relPath = `fish/search_by_common_name?name=${query}`;
    fetchApi(relPath, "GET", async (response) => setFishList(await response.json()),
      setError, setLoading)
  };

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
    setLoading(true);
    if (error) setError(null);
  }

  return (
    <div className="fish-search-container">
      <div className="fish-search-input-wrapper">
        <input
          type="text"
          value={query}
          onChange={handleInputChange}
          placeholder="Suchbegriff eingeben..."
          className="search-input"
        />
      </div>

      {loading && <Loading />}

      {error
        ? <div className="fish-search-error">{error}</div>
        : <div className="fish-search-content">
            <ul>
              {fishList.map((fish) =>
                <li key={`${fish.commonName}-${fish.scientificName}`}>
                  {`${fish.commonName}: ${fish.scientificName}`}
                </li>
              )}
            </ul>
          </div>
      }
    </div>
  )
}

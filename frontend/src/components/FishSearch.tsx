import { useEffect, useState, type ChangeEvent } from "react";
import { useDebounce } from "../util/useDebounce";
import { Loading } from "./Loading";

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
    fetch(`http://localhost:8080/api/fish/search_by_common_name?name=${query}`, {
      method: "GET",
      credentials: "include"
    })
      .then(async response =>  {
      const responseBody = await response.json();
      if (!response.ok) {
        throw new Error(responseBody.message);
      }
      return responseBody;
    })
      .then(responseBody => setFishList(responseBody))
      .catch(err => setError(String(err)))
      .finally(() => setLoading(false))
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
                <li>{`${fish.commonName}: ${fish.scientificName}`}</li>
              )}
            </ul>
          </div>
      }
    </div>
  )
}

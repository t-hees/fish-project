import { useEffect, useState, type ChangeEvent } from "react";
import { useDebounce } from "../util/useDebounce";
import { Loading } from "./Loading";
import { fetchApi } from "../util/fetchApi";
import "./ItemAutocomplete.css";

type ItemAutocompleteType<T> = {
  url: string,
  onSelect: (item: T) => void,
  displayFunc: (item: T) => string,
  setError: (error: string|null) => void,
}
export default function ItemAutocomplete<T>({ url, onSelect, displayFunc, setError}: ItemAutocompleteType<T>) {
  const [itemList, setItemList]  = useState<T[]>([]);
  const [query, setQuery] = useState<string>("");
  const debouncedQuery = useDebounce<string>(query, 500);
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    if (debouncedQuery !== "") {
      fetchApi(url + encodeURIComponent(debouncedQuery), "GET", async (response) => setItemList(await response.json()),
        setError, setLoading)
    }
  }, [debouncedQuery, url, setError])

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
    if (e.target.value === "") {
      setItemList([]);
      setLoading(false);
    } else {
      setLoading(true);
    }
  }

  return (
    <div className="item-search-container">
      <div className="item-search-input-wrapper">
        <input
          type="text"
          value={query}
          onChange={handleInputChange}
          placeholder="Suchbegriff eingeben..."
          className="search-input"
        />
      </div>

      {loading && <Loading />}
      <ul className="item-search-list">
        {/* A search still in flight when the input got cleared may set results afterwards */}
        {query !== "" && itemList.map((item) =>
          <li
            className="item-search-list-item"
            key={displayFunc(item)}
            onClick={() => {onSelect(item); setQuery(""); setItemList([])}}
          >
            {displayFunc(item)}
          </li>
        )}
      </ul>
    </div>
  )
}

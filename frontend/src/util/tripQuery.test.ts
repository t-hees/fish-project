import { describe, expect, it } from 'vitest';
import { buildTripQuery, EMPTY_SEARCH_PARAMS } from './tripQuery';

describe("buildTripQuery", () => {
  it("only contains paging without search params", () => {
    expect(buildTripQuery(2, 10, EMPTY_SEARCH_PARAMS)).toBe("page=2&size=10");
  });

  it("contains all set search params", () => {
    const query = new URLSearchParams(buildTripQuery(0, 10, {
      location: "  cool lake ",
      environment: "LAKE",
      from: "2026-01-01",
      to: "2026-02-01",
    }));
    expect(Object.fromEntries(query)).toEqual({
      page: "0",
      size: "10",
      location: "cool lake",
      environment: "LAKE",
      from: "2026-01-01",
      to: "2026-02-01",
    });
  });

  it("omits a blank location", () => {
    expect(buildTripQuery(0, 10, { ...EMPTY_SEARCH_PARAMS, location: "   " })).toBe("page=0&size=10");
  });
});

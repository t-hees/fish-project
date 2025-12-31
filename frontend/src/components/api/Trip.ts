export const environmentList = ["RIVER", "LAKE", "OCEAN"] as const;
export const weatherList = [
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

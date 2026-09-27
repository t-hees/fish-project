import { createContext, useContext } from "react";
import { de, type Translations } from "./de";
import { en } from "./en";

// A new language only needs its translations added here
export const translations = { de, en } satisfies Record<string, Translations>;

export type Language = keyof typeof translations;

export const languages = Object.keys(translations) as Language[];
export function default_language(): Language {
  const candidates = navigator.languages?.length
    ? navigator.languages
    : [navigator.language];

  for (const raw of candidates) {
    const short = raw.split("-")[0].toLowerCase() as Language;
    if (languages.includes(short)) return short;
  }
  return "de" // fallback language
};

export type LanguageType = {
  language: Language,
  setLanguage: (language: Language) => void,
  t: Translations,
}

export const LanguageContext = createContext<LanguageType>({
  language: "de",
  setLanguage: () => {},
  t: de,
});

export function useTranslation(): LanguageType {
  const ctx = useContext(LanguageContext);
  // if (!ctx) throw new Error("useTranslation must be used with a LanguageProvider!");
  return ctx;
}

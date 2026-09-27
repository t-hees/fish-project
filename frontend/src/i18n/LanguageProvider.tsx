import { useEffect, useState, type ReactNode } from "react";
import { default_language, LanguageContext, languages, translations, type Language } from "./LanguageContext";

const STORAGE_KEY = "language";

function storedLanguage(): Language {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (languages.includes(stored as Language)) return stored as Language;
  } catch {
    // Storage can be unavailable, e.g. when blocked by the browser
  }
  return default_language();
}

export function LanguageProvider({children}: {children: ReactNode}) {
  const [language, setLanguage] = useState<Language>(storedLanguage);

  useEffect(() => {
    document.documentElement.lang = language;
    try {
      localStorage.setItem(STORAGE_KEY, language);
    } catch {
      // The language then just isn't remembered
    }
  }, [language]);

  return (
    <LanguageContext.Provider value={{language: language, setLanguage: setLanguage, t: translations[language]}}>
      {children}
    </LanguageContext.Provider>
  )
}

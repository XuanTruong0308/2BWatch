import { createContext, useContext, useEffect, useMemo, useState } from "react";

export type Language = "vi" | "en";

const STORAGE_KEY = "2bshop.language";

function resolveInitialLanguage(): Language {
  if (typeof window === "undefined") {
    return "vi";
  }

  const stored = window.localStorage.getItem(STORAGE_KEY);
  return stored === "en" ? "en" : "vi";
}

let currentLanguage: Language = resolveInitialLanguage();

function applyLanguage(language: Language) {
  currentLanguage = language;

  if (typeof window !== "undefined") {
    window.localStorage.setItem(STORAGE_KEY, language);
  }

  if (typeof document !== "undefined") {
    document.documentElement.lang = language;
  }
}

type I18nContextValue = {
  language: Language;
  setLanguage: (language: Language) => void;
  toggleLanguage: () => void;
  tx: (vi: string, en: string) => string;
};

const I18nContext = createContext<I18nContextValue | null>(null);

export function I18nProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguageState] = useState<Language>(() => resolveInitialLanguage());

  useEffect(() => {
    applyLanguage(language);
  }, [language]);

  const value = useMemo<I18nContextValue>(
    () => ({
      language,
      setLanguage: (nextLanguage) => setLanguageState(nextLanguage),
      toggleLanguage: () => setLanguageState((current) => (current === "vi" ? "en" : "vi")),
      tx: (vi, en) => (language === "vi" ? vi : en),
    }),
    [language],
  );

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n() {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error("useI18n must be used inside I18nProvider");
  }
  return context;
}

export function getCurrentLanguage(): Language {
  return currentLanguage;
}

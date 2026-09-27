import { useTranslation } from "../i18n/LanguageContext";

export function Loading() {
  const { t } = useTranslation();
  return (
    <div>
      {t.common.loading}
    </div>
  )
}

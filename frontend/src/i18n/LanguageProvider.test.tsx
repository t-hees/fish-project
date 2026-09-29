import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { Loading } from '../components/Loading';
import { LanguageProvider } from './LanguageProvider';
import { translations } from './LanguageContext';

function renderWithLanguage() {
  return render(
    <MemoryRouter>
      <LanguageProvider>
        <Navbar />
        <Loading />
      </LanguageProvider>
    </MemoryRouter>
  );
}

describe("LanguageProvider", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.spyOn(window.navigator, 'languages', 'get').mockReturnValue(['en-US', 'de-DE']);
    vi.spyOn(window.navigator, 'language', 'get').mockReturnValue('en-US');
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("defaults to english", () => {
    renderWithLanguage();

    expect(screen.getByText(translations.en.common.loading)).toBeInTheDocument();
    expect(document.documentElement.lang).toBe("en");
  });

  it("defaults to german", () => {
    vi.spyOn(window.navigator, 'languages', 'get').mockReturnValue(['de-DE', 'en-US']);
    vi.spyOn(window.navigator, 'language', 'get').mockReturnValue('de-DE');

    renderWithLanguage();

    expect(screen.getByText(translations.de.common.loading)).toBeInTheDocument();
    expect(document.documentElement.lang).toBe("de");
  });

  it("switches the language with the navbar button and remembers it", async () => {
    const { unmount } = renderWithLanguage();
    await userEvent.click(screen.getByRole("button", { name: "DE" }));

    expect(screen.getByText(translations.de.common.loading)).toBeInTheDocument();
    expect(document.documentElement.lang).toBe("de");

    unmount();
    renderWithLanguage();
    expect(screen.getByText(translations.de.common.loading)).toBeInTheDocument();

    await userEvent.click(screen.getByRole("button", { name: "EN" }));
    expect(screen.getByText(translations.en.common.loading)).toBeInTheDocument();
  });
});

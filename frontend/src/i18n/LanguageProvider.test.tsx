import { beforeEach, describe, expect, it } from 'vitest';
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
  beforeEach(() => localStorage.clear());

  it("defaults to german", () => {
    renderWithLanguage();

    expect(screen.getByText(translations.de.common.loading)).toBeInTheDocument();
    expect(document.documentElement.lang).toBe("de");
  });

  it("switches the language with the navbar button and remembers it", async () => {
    const { unmount } = renderWithLanguage();
    await userEvent.click(screen.getByRole("button", { name: "EN" }));

    expect(screen.getByText(translations.en.common.loading)).toBeInTheDocument();
    expect(document.documentElement.lang).toBe("en");

    unmount();
    renderWithLanguage();
    expect(screen.getByText(translations.en.common.loading)).toBeInTheDocument();

    await userEvent.click(screen.getByRole("button", { name: "DE" }));
    expect(screen.getByText(translations.de.common.loading)).toBeInTheDocument();
  });
});

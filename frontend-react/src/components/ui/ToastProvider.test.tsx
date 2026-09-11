import { describe, expect, it } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Routes, Route, Link } from "react-router-dom";
import { ToastProvider, useToast } from "./ToastProvider";

function TestButtons() {
  const { showError, showSuccess } = useToast();
  return (
    <div>
      <button onClick={() => showError("Erreur")}>error</button>
      <button onClick={() => showError("Erreur B")}>error-b</button>
      <button onClick={() => showSuccess("OK")}>success</button>
    </div>
  );
}

function renderWithRouter(ui: React.ReactElement) {
  return render(
    <MemoryRouter initialEntries={["/a"]}>
      <ToastProvider>{ui}</ToastProvider>
    </MemoryRouter>
  );
}

describe("ToastProvider", () => {
  it("n'empile pas plus de 3 toasts simultanement", async () => {
    const user = userEvent.setup();
    renderWithRouter(<TestButtons />);

    await user.click(screen.getByText("error"));
    await user.click(screen.getByText("error-b"));
    await user.click(screen.getByText("success"));
    await user.click(screen.getByText("error"));

    await waitFor(() => {
      expect(screen.getAllByRole("alert")).toHaveLength(3);
    });
  });

  it("ne duplique pas un message identique deja affiche", async () => {
    const user = userEvent.setup();
    renderWithRouter(<TestButtons />);

    await user.click(screen.getByText("error"));
    await user.click(screen.getByText("error"));
    await user.click(screen.getByText("error"));

    expect(screen.getAllByRole("alert")).toHaveLength(1);
    expect(screen.getByRole("alert")).toHaveTextContent("Erreur");
  });

  it("se ferme au clic", async () => {
    const user = userEvent.setup();
    renderWithRouter(<TestButtons />);

    await user.click(screen.getByText("error"));
    const toast = screen.getByRole("alert");
    await user.click(toast);

    await waitFor(() => {
      expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });
  });

  it("disparait automatiquement apres le delai", async () => {
    const user = userEvent.setup();
    renderWithRouter(<TestButtons />);

    await user.click(screen.getByText("error"));
    expect(screen.getByRole("alert")).toBeInTheDocument();

    await waitFor(
      () => {
        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
      },
      { timeout: 6000 }
    );
  }, 8000);

  it("efface les toasts en attente lors d'un changement de route (evite qu'un ancien message survive a une navigation)", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/a"]}>
        <ToastProvider>
          <Link to="/b">Aller a B</Link>
          <Routes>
            <Route path="/a" element={<TestButtons />} />
            <Route path="/b" element={<div>Page B</div>} />
          </Routes>
        </ToastProvider>
      </MemoryRouter>
    );

    await user.click(screen.getByText("error"));
    expect(screen.getByRole("alert")).toHaveTextContent("Erreur");

    await user.click(screen.getByRole("link", { name: "Aller a B" }));

    expect(await screen.findByText("Page B")).toBeInTheDocument();
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });
});

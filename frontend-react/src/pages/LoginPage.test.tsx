import { describe, expect, it, vi, beforeEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Routes, Route } from "react-router-dom";
import { renderWithProviders } from "../tests/testUtils";
import { LoginPage } from "./LoginPage";
import * as authApi from "../api/auth";
import { ApiError } from "../api/client";

vi.mock("../api/auth");

function renderLoginPage() {
  return renderWithProviders(
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/pharmacien" element={<div>Dashboard Pharmacien</div>} />
    </Routes>,
    { initialEntries: ["/login"] }
  );
}

describe("LoginPage", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("affiche le formulaire avec les champs email et mot de passe", () => {
    renderLoginPage();
    expect(screen.getByText("Gestion de Pharmacie")).toBeInTheDocument();
    expect(screen.getByLabelText("Email:")).toBeInTheDocument();
    expect(screen.getByLabelText("Mot de passe:")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Se connecter" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Créer un compte client" })).toBeInTheDocument();
  });

  it("affiche des erreurs de validation si le formulaire est vide", async () => {
    const user = userEvent.setup();
    renderLoginPage();

    await user.click(screen.getByRole("button", { name: "Se connecter" }));

    expect(await screen.findByText("L'email est requis.")).toBeInTheDocument();
    expect(authApi.login).not.toHaveBeenCalled();
  });

  it("redirige vers le tableau de bord du role apres une connexion reussie", async () => {
    vi.mocked(authApi.login).mockResolvedValue({
      id: 1,
      nom: "Jean Pharmacien",
      email: "jean@test.com",
      role: "PHARMACIEN"
    });

    const user = userEvent.setup();
    renderLoginPage();

    await user.type(screen.getByLabelText("Email:"), "jean@test.com");
    await user.type(screen.getByLabelText("Mot de passe:"), "secret123");
    await user.click(screen.getByRole("button", { name: "Se connecter" }));

    await waitFor(() => {
      expect(screen.getByText("Dashboard Pharmacien")).toBeInTheDocument();
    });
    expect(authApi.login).toHaveBeenCalledWith({ email: "jean@test.com", password: "secret123" });
  });

  it("affiche un message d'erreur clair sous le formulaire en cas d'echec (pas d'alert bloquante)", async () => {
    vi.mocked(authApi.login).mockRejectedValue(new ApiError(401, "Mot de passe incorrect.", "raw"));

    const user = userEvent.setup();
    renderLoginPage();

    await user.type(screen.getByLabelText("Email:"), "jean@test.com");
    await user.type(screen.getByLabelText("Mot de passe:"), "wrong");
    await user.click(screen.getByRole("button", { name: "Se connecter" }));

    expect(await screen.findByText("Mot de passe incorrect.")).toBeInTheDocument();
  });
});

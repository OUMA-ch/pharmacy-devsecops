import { describe, expect, it, vi, beforeEach } from "vitest";
import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Routes, Route } from "react-router-dom";
import { renderWithProviders } from "../tests/testUtils";
import { RegisterPage } from "./RegisterPage";
import * as authApi from "../api/auth";
import { ApiError } from "../api/client";
import { PASSWORD_RULE } from "../lib/validationSchemas";

vi.mock("../api/auth");

function renderRegisterPage() {
  return renderWithProviders(
    <Routes>
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<div>Page de connexion</div>} />
    </Routes>,
    { initialEntries: ["/register"] }
  );
}

async function remplir(password: string) {
  const user = userEvent.setup();
  await user.type(screen.getByLabelText(/Nom complet/), "Client Test");
  await user.type(screen.getByLabelText(/Email/), "client@test.com");
  await user.type(screen.getByLabelText(/Téléphone/), "0612345678");
  await user.type(screen.getByLabelText(/Mot de passe/), password);
  await user.click(screen.getByRole("button", { name: "S'inscrire" }));
}

describe("RegisterPage - politique de mot de passe", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("affiche la regle sous le champ mot de passe", () => {
    renderRegisterPage();
    expect(screen.getByText(PASSWORD_RULE)).toBeInTheDocument();
  });

  it("bloque cote client un mot de passe faible sans appeler le serveur", async () => {
    renderRegisterPage();
    await remplir("123456789");

    expect(await screen.findByRole("alert")).toHaveTextContent(PASSWORD_RULE);
    expect(authApi.registerClient).not.toHaveBeenCalled();
  });

  it("affiche sous le champ l'erreur password renvoyee par le serveur", async () => {
    const messageServeur = "Message du serveur pour password.";
    vi.mocked(authApi.registerClient).mockRejectedValue(
      new ApiError(400, messageServeur, "HTTP 400", { password: messageServeur })
    );

    renderRegisterPage();
    await remplir("Test1234");

    expect(await screen.findByRole("alert")).toHaveTextContent(messageServeur);
    expect(screen.queryByText(PASSWORD_RULE)).not.toBeInTheDocument();
  });

  it("redirige vers la connexion avec un mot de passe conforme", async () => {
    vi.mocked(authApi.registerClient).mockResolvedValue({});

    renderRegisterPage();
    await remplir("Test1234");

    expect(await screen.findByText("Page de connexion")).toBeInTheDocument();
  });
});

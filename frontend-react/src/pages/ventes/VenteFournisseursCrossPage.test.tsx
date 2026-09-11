import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, Routes, Route, Link } from "react-router-dom";
import { ToastProvider } from "../../components/ui/ToastProvider";
import { ApiError } from "../../api/client";
import { FournisseursPage } from "../fournisseurs/FournisseursPage";
import { VenteFormModal } from "./VenteFormModal";
import * as fournisseursApi from "../../api/fournisseurs";
import * as fournituresApi from "../../api/fournitures";
import * as ventesApi from "../../api/ventes";
import * as ordonnancesApi from "../../api/ordonnances";

vi.mock("../../api/fournisseurs");
vi.mock("../../api/fournitures");
vi.mock("../../api/ventes");
vi.mock("../../api/ordonnances");

/**
 * Reproduit fidelement le scenario signale : une suppression Fournisseur
 * refusee (toast "Cette suppression est impossible...") PUIS, apres une VRAIE
 * navigation React Router (pas un remontage du test), une ordonnance perimee
 * sur la page Ventes (attendu : toast "Ordonnance expirée..."). Comme dans
 * App.tsx, QueryClientProvider/ToastProvider/BrowserRouter englobent les
 * <Routes> et ne se demontent jamais entre deux pages : seul le contenu de la
 * route change. Une precedente version de ce test utilisait deux appels
 * render() separes, ce qui recreait un ToastProvider frais a chaque fois et ne
 * reproduisait donc pas le partage d'etat reel entre pages.
 */
function VentesRoutePage() {
  return <VenteFormModal open onClose={() => {}} />;
}

function Harness() {
  return (
    <>
      <Link to="/ventes">Aller aux ventes</Link>
      <Routes>
        <Route path="/fournisseurs" element={<FournisseursPage />} />
        <Route path="/ventes" element={<VentesRoutePage />} />
      </Routes>
    </>
  );
}

describe("Ventes apres une erreur Fournisseurs (etat du Toast partage entre pages)", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("affiche le toast Ordonnance expiree et pas l'ancien message Fournisseurs", async () => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false }, mutations: { retry: false } }
    });

    vi.mocked(fournisseursApi.listFournisseurs).mockResolvedValue([
      { idFournisseur: 1, nomFournisseur: "Fournisseur A", tel: "0600000000" }
    ]);
    vi.mocked(fournituresApi.listFournituresByFournisseur).mockResolvedValue([]);
    vi.mocked(fournisseursApi.deleteFournisseur).mockRejectedValue(
      new ApiError(
        400,
        "Cette suppression est impossible car cet element est encore utilise ailleurs dans l'application.",
        "DataIntegrityViolationException: could not execute statement"
      )
    );

    const user = userEvent.setup();
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={["/fournisseurs"]}>
          <ToastProvider>
            <Harness />
          </ToastProvider>
        </MemoryRouter>
      </QueryClientProvider>
    );

    expect(await screen.findByText("Fournisseur A")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Supprimer" }));
    const confirmDialog = await screen.findByRole("dialog");
    await user.click(within(confirmDialog).getByRole("button", { name: "Supprimer" }));

    expect(
      await screen.findByText(
        "Cette suppression est impossible car cet element est encore utilise ailleurs dans l'application."
      )
    ).toBeInTheDocument();

    // Navigation reelle React Router : FournisseursPage se demonte, mais
    // ToastProvider (et son etat "toasts") ne se demonte jamais, exactement
    // comme dans App.tsx.
    vi.mocked(ordonnancesApi.createOrdonnance).mockResolvedValue({
      idOrdonnance: 7,
      dateEmission: "2025-01-01",
      nomMedecin: "Dr House",
      description: "RAS",
      clientId: 1
    });
    vi.mocked(ventesApi.createVente).mockRejectedValue(
      new ApiError(400, "Ordonnance expirée (plus de 3 mois)", "Ordonnance expirée (plus de 3 mois)")
    );

    await user.click(screen.getByRole("link", { name: "Aller aux ventes" }));
    expect(await screen.findByText("Nouvelle Vente")).toBeInTheDocument();

    await user.type(screen.getByLabelText(/^Client ID:/), "1");
    await user.type(screen.getByLabelText(/^Produit ID:/), "5");
    await user.type(screen.getByLabelText(/^Quantité:/), "3");
    await user.click(screen.getByLabelText("Avec ordonnance ?"));
    await user.click(screen.getByRole("button", { name: "Valider" }));

    expect(await screen.findByText("Nouvelle Ordonnance")).toBeInTheDocument();
    await user.type(screen.getByLabelText(/^Médecin:/), "Dr House");
    await user.type(screen.getByLabelText(/^Date émission:/), "2025-01-01");
    await user.click(screen.getByRole("button", { name: "Valider" }));

    await waitFor(() => {
      expect(screen.getByText("Ordonnance expirée (plus de 3 mois)")).toBeInTheDocument();
    });
    expect(
      screen.queryByText(
        "Cette suppression est impossible car cet element est encore utilise ailleurs dans l'application."
      )
    ).not.toBeInTheDocument();
  });
});

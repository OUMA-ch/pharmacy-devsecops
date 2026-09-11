import { describe, expect, it, vi, beforeEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "../../tests/testUtils";
import { VenteFormModal } from "./VenteFormModal";
import * as ventesApi from "../../api/ventes";
import * as ordonnancesApi from "../../api/ordonnances";
import { ApiError } from "../../api/client";

vi.mock("../../api/ventes");
vi.mock("../../api/ordonnances");

describe("VenteFormModal", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("desactive Valider tant que Client ID / Produit ID / Quantite ne sont pas renseignes", () => {
    renderWithProviders(<VenteFormModal open onClose={vi.fn()} />);
    expect(screen.getByRole("button", { name: "Valider" })).toBeDisabled();
  });

  it("enregistre directement la vente sans ordonnance si la case n'est pas cochee", async () => {
    vi.mocked(ventesApi.createVente).mockResolvedValue({
      idVente: 1,
      quantite: 2,
      dateVente: "2026-01-01",
      clientId: 1,
      produitId: 5,
      ordonnanceId: null,
      prixTotal: 20,
      ordonnanceNomMedecin: null,
      ordonnanceDateEmission: null,
      ordonnanceDescription: null
    });

    const onClose = vi.fn();
    const user = userEvent.setup();
    renderWithProviders(<VenteFormModal open onClose={onClose} />);

    await user.type(screen.getByLabelText(/^Client ID:/), "1");
    await user.type(screen.getByLabelText(/^Produit ID:/), "5");
    await user.type(screen.getByLabelText(/^Quantité:/), "2");
    await user.click(screen.getByRole("button", { name: "Valider" }));

    await waitFor(() => {
      expect(ventesApi.createVente).toHaveBeenCalledWith(
        expect.objectContaining({ clientId: 1, produitId: 5, quantite: 2, ordonnanceId: null }),
        expect.anything()
      );
    });
    expect(ordonnancesApi.createOrdonnance).not.toHaveBeenCalled();
    await waitFor(() => expect(onClose).toHaveBeenCalled());
  });

  it("ouvre automatiquement la modale Nouvelle Ordonnance si la case est cochee, puis cree ordonnance + vente", async () => {
    vi.mocked(ordonnancesApi.createOrdonnance).mockResolvedValue({
      idOrdonnance: 42,
      dateEmission: "2026-01-01",
      nomMedecin: "Dr House",
      description: "RAS",
      clientId: 1
    });
    vi.mocked(ventesApi.createVente).mockResolvedValue({
      idVente: 2,
      quantite: 3,
      dateVente: "2026-01-01",
      clientId: 1,
      produitId: 5,
      ordonnanceId: 42,
      prixTotal: 30,
      ordonnanceNomMedecin: "Dr House",
      ordonnanceDateEmission: "2026-01-01",
      ordonnanceDescription: "RAS"
    });

    const onClose = vi.fn();
    const user = userEvent.setup();
    renderWithProviders(<VenteFormModal open onClose={onClose} />);

    await user.type(screen.getByLabelText(/^Client ID:/), "1");
    await user.type(screen.getByLabelText(/^Produit ID:/), "5");
    await user.type(screen.getByLabelText(/^Quantité:/), "3");
    await user.click(screen.getByLabelText("Avec ordonnance ?"));
    await user.click(screen.getByRole("button", { name: "Valider" }));

    expect(await screen.findByText("Nouvelle Ordonnance")).toBeInTheDocument();
    expect(ventesApi.createVente).not.toHaveBeenCalled();

    await user.type(screen.getByLabelText(/^Médecin:/), "Dr House");
    await user.type(screen.getByLabelText(/^Date émission:/), "2026-01-01");

    const validerOrdonnance = screen.getAllByRole("button", { name: "Valider" })[0]!;
    await user.click(validerOrdonnance);

    await waitFor(() => {
      expect(ordonnancesApi.createOrdonnance).toHaveBeenCalledWith(
        expect.objectContaining({ clientId: 1, nomMedecin: "Dr House", dateEmission: "2026-01-01" })
      );
    });
    await waitFor(() => {
      expect(ventesApi.createVente).toHaveBeenCalledWith(
        expect.objectContaining({ ordonnanceId: 42 })
      );
    });
    await waitFor(() => expect(onClose).toHaveBeenCalled());
  });

  it("affiche le toast d'erreur si le backend refuse l'ordonnance (expirée) apres creation ordonnance+vente", async () => {
    vi.mocked(ordonnancesApi.createOrdonnance).mockResolvedValue({
      idOrdonnance: 99,
      dateEmission: "2025-01-01",
      nomMedecin: "Dr House",
      description: "RAS",
      clientId: 1
    });
    vi.mocked(ventesApi.createVente).mockRejectedValue(
      new ApiError(400, "Ordonnance expirée (plus de 3 mois)", "Ordonnance expirée (plus de 3 mois)")
    );

    const onClose = vi.fn();
    const user = userEvent.setup();
    renderWithProviders(<VenteFormModal open onClose={onClose} />);

    await user.type(screen.getByLabelText(/^Client ID:/), "1");
    await user.type(screen.getByLabelText(/^Produit ID:/), "5");
    await user.type(screen.getByLabelText(/^Quantité:/), "3");
    await user.click(screen.getByLabelText("Avec ordonnance ?"));
    await user.click(screen.getByRole("button", { name: "Valider" }));

    expect(await screen.findByText("Nouvelle Ordonnance")).toBeInTheDocument();

    await user.type(screen.getByLabelText(/^Médecin:/), "Dr House");
    await user.type(screen.getByLabelText(/^Date émission:/), "2025-01-01");
    await user.click(screen.getByRole("button", { name: "Valider" }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Ordonnance expirée (plus de 3 mois)"
    );
    expect(onClose).not.toHaveBeenCalled();
    expect(screen.getByText("Nouvelle Ordonnance")).toBeInTheDocument();
  });
});

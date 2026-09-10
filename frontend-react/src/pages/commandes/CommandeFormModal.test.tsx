import { describe, expect, it, vi, beforeEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "../../tests/testUtils";
import { CommandeFormModal } from "./CommandeFormModal";
import * as commandesApi from "../../api/commandes";

vi.mock("../../api/commandes");

describe("CommandeFormModal", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("affiche l'erreur exacte si on ajoute une ligne sans Produit ID/Quantite valides", async () => {
    const user = userEvent.setup();
    renderWithProviders(<CommandeFormModal open onClose={vi.fn()} />);

    await user.click(screen.getByRole("button", { name: "Ajouter ligne" }));

    expect(
      await screen.findByText("Produit ID et Quantité valides obligatoires.")
    ).toBeInTheDocument();
  });

  it("affiche la meme erreur si on clique Creer sans aucune ligne", async () => {
    const user = userEvent.setup();
    renderWithProviders(<CommandeFormModal open onClose={vi.fn()} />);

    await user.type(screen.getByLabelText(/^Fournisseur ID:/), "1");
    await user.click(screen.getByRole("button", { name: "Créer" }));

    expect(
      await screen.findByText("Produit ID et Quantité valides obligatoires.")
    ).toBeInTheDocument();
    expect(commandesApi.createCommande).not.toHaveBeenCalled();
  });

  it("ajoute une ligne valide au mini-tableau puis cree la commande avec ses lignes", async () => {
    vi.mocked(commandesApi.createCommande).mockResolvedValue({
      idCommande: 1,
      dateCommande: "2026-01-01",
      statut: "EN_ATTENTE",
      fournisseurId: 1,
      lignes: [{ produitId: 7, quantiteDemande: 10 }]
    });

    const onClose = vi.fn();
    const user = userEvent.setup();
    renderWithProviders(<CommandeFormModal open onClose={onClose} />);

    await user.type(screen.getByLabelText(/^Fournisseur ID:/), "1");
    await user.type(screen.getByLabelText("Produit ID"), "7");
    await user.type(screen.getByLabelText("Quantité demandée"), "10");
    await user.click(screen.getByRole("button", { name: "Ajouter ligne" }));

    expect(await screen.findByText("7")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Créer" }));

    await waitFor(() => {
      expect(commandesApi.createCommande).toHaveBeenCalledWith(
        expect.objectContaining({
          fournisseurId: 1,
          lignes: [{ produitId: 7, quantiteDemande: 10 }]
        }),
        expect.anything()
      );
    });
    await waitFor(() => expect(onClose).toHaveBeenCalled());
  });
});

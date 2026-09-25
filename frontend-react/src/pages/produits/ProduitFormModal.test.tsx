import { describe, expect, it, vi, beforeEach } from "vitest";
import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "../../tests/testUtils";
import { ProduitFormModal } from "./ProduitFormModal";
import * as produitsApi from "../../api/produits";
import { ApiError } from "../../api/client";

vi.mock("../../api/produits");

describe("ProduitFormModal", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it("desactive le bouton Valider tant que le nom est vide (comportement observe)", () => {
    renderWithProviders(<ProduitFormModal open produit={null} onClose={vi.fn()} />);
    expect(screen.getByRole("button", { name: "Valider" })).toBeDisabled();
  });

  it("active Valider une fois le nom saisi et cree le produit avec les bons champs", async () => {
    vi.mocked(produitsApi.createProduit).mockResolvedValue({
      idProduit: 1,
      nomCommercial: "Doliprane",
      composition: "Paracetamol",
      prixP: 12.5,
      formPharmaceutique: null,
      dosage: null,
      datePeremption: "2027-01-01",
      quantiteStock: 50
    });

    const onClose = vi.fn();
    const user = userEvent.setup();
    renderWithProviders(<ProduitFormModal open produit={null} onClose={onClose} />);

    await user.type(screen.getByLabelText(/^Nom:/), "Doliprane");
    await user.type(screen.getByLabelText("Description:"), "Paracetamol");
    await user.clear(screen.getByLabelText(/^Prix:/));
    await user.type(screen.getByLabelText(/^Prix:/), "12.5");
    await user.clear(screen.getByLabelText(/^Stock:/));
    await user.type(screen.getByLabelText(/^Stock:/), "50");
    await user.type(screen.getByLabelText(/^Date péremption:/), "2027-01-01");

    const validerBtn = screen.getByRole("button", { name: "Valider" });
    expect(validerBtn).toBeEnabled();
    await user.click(validerBtn);

    await waitFor(() => {
      expect(produitsApi.createProduit).toHaveBeenCalledWith(
        expect.objectContaining({
          nomCommercial: "Doliprane",
          composition: "Paracetamol",
          prixP: 12.5,
          quantiteStock: 50,
          datePeremption: "2027-01-01"
        })
      );
    });
    await waitFor(() => expect(onClose).toHaveBeenCalled());
  });

  it("affiche sous le champ Prix l'erreur de validation 400 renvoyee par le backend", async () => {
    vi.mocked(produitsApi.createProduit).mockRejectedValue(
      new ApiError(400, "Le prix doit être positif ou nul.", "HTTP 400", {
        prixP: "Le prix doit être positif ou nul."
      })
    );

    const onClose = vi.fn();
    const user = userEvent.setup();
    renderWithProviders(<ProduitFormModal open produit={null} onClose={onClose} />);

    await user.type(screen.getByLabelText(/^Nom:/), "Doliprane");
    await user.type(screen.getByLabelText(/^Date péremption:/), "2027-01-01");
    await user.click(screen.getByRole("button", { name: "Valider" }));

    // Le message doit etre dans le bloc du champ Prix (FormField), pas seulement dans le toast.
    const prixInput = screen.getByLabelText(/^Prix:/);
    await waitFor(() =>
      expect(prixInput.parentElement).toHaveTextContent("Le prix doit être positif ou nul.")
    );
    expect(onClose).not.toHaveBeenCalled();
  });
});

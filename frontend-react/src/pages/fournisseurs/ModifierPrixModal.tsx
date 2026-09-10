import { useState, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import * as fournituresApi from "../../api/fournitures";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { Modal } from "../../components/ui/Modal";
import { Button } from "../../components/ui/Button";
import { inputClassName } from "../../components/ui/FormField";
import type { FournitureDTO } from "../../types/api";

interface ModifierPrixModalProps {
  open: boolean;
  fourniture: FournitureDTO | null;
  idFournisseur: number;
  onClose: () => void;
}

export function ModifierPrixModal({
  open,
  fourniture,
  idFournisseur,
  onClose
}: ModifierPrixModalProps) {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();
  const [prix, setPrix] = useState("");

  useEffect(() => {
    if (open && fourniture) setPrix(String(fourniture.prixAchat));
  }, [open, fourniture]);

  const mutation = useMutation({
    mutationFn: (nouveauPrix: number) =>
      fournituresApi.modifierPrixFourniture(fourniture!.idProduit, idFournisseur, nouveauPrix),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fournitures", idFournisseur] });
      showSuccess("Prix d'achat mis à jour.");
      onClose();
    },
    onError: (err) => showError(err instanceof ApiError ? err.message : "Une erreur est survenue.")
  });

  const parsedPrix = Number(prix);
  const isValid = prix.trim() !== "" && Number.isFinite(parsedPrix) && parsedPrix >= 0;

  return (
    <Modal
      open={open}
      title="Modifier Prix Achat"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button
            variant="primary"
            disabled={!isValid || mutation.isPending}
            onClick={() => mutation.mutate(parsedPrix)}
          >
            OK
          </Button>
        </>
      }
    >
      <p className="mb-3 text-sm text-slate-600">
        Produit ID: <span className="font-semibold">{fourniture?.idProduit}</span>
      </p>

      <label
        htmlFor="nouveau-prix"
        className="mb-1 flex items-center gap-1 text-sm font-medium text-slate-700"
      >
        Nouveau prix achat:
        <span
          tabIndex={0}
          role="img"
          aria-label="Aide"
          title="Prix auquel la pharmacie achète ce produit à ce fournisseur, utilisé pour calculer les marges."
          className="cursor-help text-xs text-slate-400"
        >
          ❓
        </span>
      </label>
      <input
        id="nouveau-prix"
        type="number"
        step="0.01"
        min="0"
        value={prix}
        onChange={(e) => setPrix(e.target.value)}
        className={inputClassName}
      />
    </Modal>
  );
}

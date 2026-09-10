import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import * as commandesApi from "../../api/commandes";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { Modal } from "../../components/ui/Modal";
import { Button } from "../../components/ui/Button";
import { ErrorDialog } from "../../components/ui/ErrorDialog";
import { FormField, inputClassName } from "../../components/ui/FormField";
import { DataTable, type Column } from "../../components/ui/DataTable";
import { commandeSchema, type CommandeFormValues } from "../../lib/validationSchemas";
import { STATUTS_COMMANDE } from "../../lib/constants";
import type { LigneCommandeDTO } from "../../types/api";

interface CommandeFormModalProps {
  open: boolean;
  onClose: () => void;
}

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

const LIGNE_VALIDATION_ERROR = "Produit ID et Quantité valides obligatoires.";

export function CommandeFormModal({ open, onClose }: CommandeFormModalProps) {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();

  const [lignes, setLignes] = useState<LigneCommandeDTO[]>([]);
  const [selectedLigneIndex, setSelectedLigneIndex] = useState<number | null>(null);
  const [ligneProduitId, setLigneProduitId] = useState("");
  const [ligneQuantite, setLigneQuantite] = useState("");
  const [validationError, setValidationError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors }
  } = useForm<CommandeFormValues>({
    resolver: zodResolver(commandeSchema),
    defaultValues: { fournisseurId: undefined, dateCommande: todayIso(), statut: "EN_ATTENTE" }
  });

  useEffect(() => {
    if (open) {
      reset({ fournisseurId: undefined, dateCommande: todayIso(), statut: "EN_ATTENTE" });
      setLignes([]);
      setSelectedLigneIndex(null);
      setLigneProduitId("");
      setLigneQuantite("");
    }
  }, [open, reset]);

  const mutation = useMutation({
    mutationFn: commandesApi.createCommande,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["commandes"] });
      showSuccess("Commande créée avec succès.");
      onClose();
    },
    onError: (err) => {
      showError(err instanceof ApiError ? err.message : "Impossible de créer la commande.");
    }
  });

  function handleAjouterLigne() {
    const produitId = Number(ligneProduitId);
    const quantiteDemande = Number(ligneQuantite);
    if (
      !ligneProduitId ||
      !ligneQuantite ||
      !Number.isInteger(produitId) ||
      produitId <= 0 ||
      !Number.isInteger(quantiteDemande) ||
      quantiteDemande <= 0
    ) {
      setValidationError(LIGNE_VALIDATION_ERROR);
      return;
    }
    setLignes((prev) => [...prev, { produitId, quantiteDemande }]);
    setLigneProduitId("");
    setLigneQuantite("");
  }

  function handleSupprimerLigne() {
    if (selectedLigneIndex === null) return;
    setLignes((prev) => prev.filter((_, idx) => idx !== selectedLigneIndex));
    setSelectedLigneIndex(null);
  }

  function onSubmitCommande(values: CommandeFormValues) {
    if (lignes.length === 0) {
      setValidationError(LIGNE_VALIDATION_ERROR);
      return;
    }
    mutation.mutate({ ...values, lignes });
  }

  const fournisseurId = watch("fournisseurId");

  const columns: Column<{ produitId: number; quantiteDemande: number; __index: number }>[] = [
    { key: "produitId", header: "Produit ID", render: (l) => l.produitId },
    { key: "quantite", header: "Quantité demandée", render: (l) => l.quantiteDemande }
  ];

  return (
    <>
      <Modal open={open} title="Nouvelle Commande" onClose={onClose} widthClassName="max-w-2xl">
        <form onSubmit={handleSubmit(onSubmitCommande)} noValidate>
          <div className="grid grid-cols-1 gap-x-4 sm:grid-cols-2">
            <FormField
              label="Fournisseur ID:"
              htmlFor="commande-fournisseur"
              error={errors.fournisseurId?.message}
              required
            >
              <input
                id="commande-fournisseur"
                type="number"
                min={1}
                className={inputClassName}
                {...register("fournisseurId")}
              />
            </FormField>

            <FormField label="Statut:" htmlFor="commande-statut" error={errors.statut?.message}>
              <select id="commande-statut" className={inputClassName} {...register("statut")}>
                {STATUTS_COMMANDE.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </FormField>
          </div>

          <h3 className="mb-2 mt-2 text-sm font-semibold text-slate-700">Lignes:</h3>
          <DataTable
            columns={columns}
            data={lignes.map((l, idx) => ({ ...l, __index: idx }))}
            getRowId={(l) => l.__index}
            selectedRowId={selectedLigneIndex}
            onRowClick={(l) => setSelectedLigneIndex(l.__index)}
          />

          <div className="mt-3 flex flex-wrap items-end gap-2">
            <div className="flex flex-col">
              <label htmlFor="ligne-produit-id" className="mb-1 text-xs font-medium text-slate-600">
                Produit ID
              </label>
              <input
                id="ligne-produit-id"
                type="number"
                min={1}
                value={ligneProduitId}
                onChange={(e) => setLigneProduitId(e.target.value)}
                className={`${inputClassName} w-32`}
              />
            </div>
            <div className="flex flex-col">
              <label htmlFor="ligne-quantite" className="mb-1 text-xs font-medium text-slate-600">
                Quantité demandée
              </label>
              <input
                id="ligne-quantite"
                type="number"
                min={1}
                value={ligneQuantite}
                onChange={(e) => setLigneQuantite(e.target.value)}
                className={`${inputClassName} w-32`}
              />
            </div>
            <Button type="button" variant="secondary" onClick={handleAjouterLigne}>
              Ajouter ligne
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={handleSupprimerLigne}
              disabled={selectedLigneIndex === null}
            >
              Supprimer ligne
            </Button>
          </div>

          <div className="mt-5 flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" disabled={!fournisseurId || mutation.isPending}>
              Créer
            </Button>
          </div>
        </form>
      </Modal>

      <ErrorDialog
        open={validationError !== null}
        message={validationError ?? ""}
        onClose={() => setValidationError(null)}
      />
    </>
  );
}

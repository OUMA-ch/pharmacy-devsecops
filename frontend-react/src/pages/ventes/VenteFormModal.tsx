import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import * as ventesApi from "../../api/ventes";
import * as ordonnancesApi from "../../api/ordonnances";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { Modal } from "../../components/ui/Modal";
import { Button } from "../../components/ui/Button";
import { FormField, inputClassName } from "../../components/ui/FormField";
import {
  venteSchema,
  type VenteFormValues,
  type OrdonnanceFormValues
} from "../../lib/validationSchemas";
import { OrdonnanceFormModal } from "./OrdonnanceFormModal";

interface VenteFormModalProps {
  open: boolean;
  onClose: () => void;
}

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

/**
 * Modale "Nouvelle Vente". Si "Avec ordonnance ?" est coche, cliquer sur
 * "Valider" ouvre automatiquement la modale "Nouvelle Ordonnance" ; l'ordonnance
 * est creee en premier, puis la vente est enregistree avec l'ordonnanceId obtenu.
 * (Hypothese documentee : le backend ne propose pas de date de vente dans ce
 * formulaire, elle est fixee automatiquement a aujourd'hui.)
 */
export function VenteFormModal({ open, onClose }: VenteFormModalProps) {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();
  const [pendingVente, setPendingVente] = useState<VenteFormValues | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors }
  } = useForm<VenteFormValues>({
    resolver: zodResolver(venteSchema),
    defaultValues: {
      clientId: undefined,
      produitId: undefined,
      quantite: undefined,
      avecOrdonnance: false
    }
  });

  useEffect(() => {
    if (open) {
      reset({
        clientId: undefined,
        produitId: undefined,
        quantite: undefined,
        avecOrdonnance: false
      });
      setPendingVente(null);
    }
  }, [open, reset]);

  const createVenteMutation = useMutation({
    mutationFn: ventesApi.createVente,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["ventes"] });
      showSuccess("Vente enregistrée avec succès.");
      setPendingVente(null);
      onClose();
    },
    onError: (err) => {
      showError(err instanceof ApiError ? err.message : "Impossible d'enregistrer la vente.");
    }
  });

  const createOrdonnanceAndVenteMutation = useMutation({
    mutationFn: async ({
      vente,
      ordonnance
    }: {
      vente: VenteFormValues;
      ordonnance: OrdonnanceFormValues;
    }) => {
      const createdOrdonnance = await ordonnancesApi.createOrdonnance({
        clientId: vente.clientId,
        dateEmission: ordonnance.dateEmission,
        nomMedecin: ordonnance.nomMedecin,
        description: ordonnance.description
      });
      return ventesApi.createVente({
        clientId: vente.clientId,
        produitId: vente.produitId,
        quantite: vente.quantite,
        dateVente: todayIso(),
        ordonnanceId: createdOrdonnance.idOrdonnance
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["ventes"] });
      showSuccess("Ordonnance et vente enregistrées avec succès.");
      setPendingVente(null);
      onClose();
    },
    onError: (err) => {
      showError(
        err instanceof ApiError ? err.message : "Impossible d'enregistrer l'ordonnance/la vente."
      );
    }
  });

  function onSubmitVente(values: VenteFormValues) {
    if (values.avecOrdonnance) {
      setPendingVente(values);
      return;
    }
    createVenteMutation.mutate({
      clientId: values.clientId,
      produitId: values.produitId,
      quantite: values.quantite,
      dateVente: todayIso(),
      ordonnanceId: null
    });
  }

  const produitId = watch("produitId");
  const quantite = watch("quantite");
  const clientId = watch("clientId");
  const canSubmit = Boolean(clientId) && Boolean(produitId) && Boolean(quantite);

  return (
    <>
      <Modal open={open && pendingVente === null} title="Nouvelle Vente" onClose={onClose}>
        <form onSubmit={handleSubmit(onSubmitVente)} noValidate>
          <FormField
            label="Client ID:"
            htmlFor="vente-client-id"
            error={errors.clientId?.message}
            required
          >
            <input
              id="vente-client-id"
              type="number"
              min={1}
              className={inputClassName}
              {...register("clientId")}
            />
          </FormField>

          <FormField
            label="Produit ID:"
            htmlFor="vente-produit-id"
            error={errors.produitId?.message}
            required
          >
            <input
              id="vente-produit-id"
              type="number"
              min={1}
              className={inputClassName}
              {...register("produitId")}
            />
          </FormField>

          <FormField
            label="Quantité:"
            htmlFor="vente-quantite"
            error={errors.quantite?.message}
            required
          >
            <input
              id="vente-quantite"
              type="number"
              min={1}
              placeholder="Quantité"
              className={inputClassName}
              {...register("quantite")}
            />
          </FormField>

          <label className="mb-4 flex items-center gap-2 text-sm text-slate-700">
            <input
              type="checkbox"
              className="h-4 w-4 rounded border-slate-300"
              {...register("avecOrdonnance")}
            />
            Avec ordonnance ?
          </label>

          <div className="flex justify-end gap-2">
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={!canSubmit || createVenteMutation.isPending}
            >
              Valider
            </Button>
          </div>
        </form>
      </Modal>

      <OrdonnanceFormModal
        open={pendingVente !== null}
        onCancel={() => setPendingVente(null)}
        isSubmitting={createOrdonnanceAndVenteMutation.isPending}
        onSubmit={(ordonnanceValues) => {
          if (!pendingVente) return;
          createOrdonnanceAndVenteMutation.mutate({
            vente: pendingVente,
            ordonnance: ordonnanceValues
          });
        }}
      />
    </>
  );
}

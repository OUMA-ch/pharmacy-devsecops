import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import * as fournituresApi from "../../api/fournitures";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { Modal } from "../../components/ui/Modal";
import { Button } from "../../components/ui/Button";
import { FormField, inputClassName } from "../../components/ui/FormField";
import { fournitureSchema, type FournitureFormValues } from "../../lib/validationSchemas";

interface FournitureFormModalProps {
  open: boolean;
  idFournisseur: number;
  onClose: () => void;
}

/** Modale "Ajouter Fourniture (Produit vendu par ce fournisseur)". */
export function FournitureFormModal({ open, idFournisseur, onClose }: FournitureFormModalProps) {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting }
  } = useForm<FournitureFormValues>({
    resolver: zodResolver(fournitureSchema),
    defaultValues: { produitId: undefined, prixAchat: 0 }
  });

  useEffect(() => {
    if (open) reset({ produitId: undefined, prixAchat: 0 });
  }, [open, reset]);

  const mutation = useMutation({
    mutationFn: (values: FournitureFormValues) =>
      fournituresApi.ajouterFourniture(values.produitId, idFournisseur, values.prixAchat),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fournitures", idFournisseur] });
      showSuccess("Produit ajouté aux fournitures de ce fournisseur.");
      onClose();
    },
    onError: (err) => showError(err instanceof ApiError ? err.message : "Une erreur est survenue.")
  });

  const produitId = watch("produitId");

  return (
    <Modal
      open={open}
      title="Ajouter Fourniture (Produit vendu par ce fournisseur)"
      onClose={onClose}
    >
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} noValidate>
        <FormField
          label="Produit ID:"
          htmlFor="fourniture-produit-id"
          error={errors.produitId?.message}
          required
        >
          <input
            id="fourniture-produit-id"
            type="number"
            min={1}
            className={inputClassName}
            {...register("produitId")}
          />
        </FormField>
        <FormField
          label="Prix achat:"
          htmlFor="fourniture-prix"
          error={errors.prixAchat?.message}
          required
        >
          <input
            id="fourniture-prix"
            type="number"
            step="0.01"
            min="0"
            className={inputClassName}
            {...register("prixAchat")}
          />
        </FormField>
        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" disabled={!produitId || isSubmitting}>
            Valider
          </Button>
        </div>
      </form>
    </Modal>
  );
}

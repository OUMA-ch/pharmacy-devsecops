import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import * as fournisseursApi from "../../api/fournisseurs";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { Modal } from "../../components/ui/Modal";
import { Button } from "../../components/ui/Button";
import { FormField, inputClassName } from "../../components/ui/FormField";
import { fournisseurSchema, type FournisseurFormValues } from "../../lib/validationSchemas";
import type { Fournisseur } from "../../types/api";

interface FournisseurFormModalProps {
  open: boolean;
  fournisseur: Fournisseur | null;
  onClose: () => void;
}

export function FournisseurFormModal({ open, fournisseur, onClose }: FournisseurFormModalProps) {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();
  const isEdit = fournisseur !== null;

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting }
  } = useForm<FournisseurFormValues>({
    resolver: zodResolver(fournisseurSchema),
    defaultValues: { nomFournisseur: "", tel: "" }
  });

  useEffect(() => {
    if (open) {
      reset(
        fournisseur
          ? { nomFournisseur: fournisseur.nomFournisseur, tel: fournisseur.tel ?? "" }
          : { nomFournisseur: "", tel: "" }
      );
    }
  }, [open, fournisseur, reset]);

  const mutation = useMutation({
    mutationFn: (values: FournisseurFormValues) => {
      const payload = { nomFournisseur: values.nomFournisseur, tel: values.tel ?? null };
      return isEdit
        ? fournisseursApi.updateFournisseur(fournisseur.idFournisseur, payload)
        : fournisseursApi.createFournisseur(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fournisseurs"] });
      showSuccess(isEdit ? "Fournisseur modifié." : "Fournisseur ajouté.");
      onClose();
    },
    onError: (err) => showError(err instanceof ApiError ? err.message : "Une erreur est survenue.")
  });

  const nom = watch("nomFournisseur");

  return (
    <Modal
      open={open}
      title={isEdit ? "Modifier Fournisseur" : "Ajouter Fournisseur"}
      onClose={onClose}
    >
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} noValidate>
        <FormField
          label="Nom:"
          htmlFor="fournisseur-nom"
          error={errors.nomFournisseur?.message}
          required
        >
          <input
            id="fournisseur-nom"
            type="text"
            className={inputClassName}
            {...register("nomFournisseur")}
          />
        </FormField>
        <FormField label="Téléphone:" htmlFor="fournisseur-tel" error={errors.tel?.message}>
          <input id="fournisseur-tel" type="tel" className={inputClassName} {...register("tel")} />
        </FormField>
        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={!nom || nom.trim() === "" || isSubmitting}
          >
            Valider
          </Button>
        </div>
      </form>
    </Modal>
  );
}

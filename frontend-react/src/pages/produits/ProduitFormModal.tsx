import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import * as produitsApi from "../../api/produits";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { Modal } from "../../components/ui/Modal";
import { Button } from "../../components/ui/Button";
import { FormField, inputClassName } from "../../components/ui/FormField";
import { DatePickerField } from "../../components/ui/DatePickerField";
import { produitSchema, type ProduitFormValues } from "../../lib/validationSchemas";
import type { Produit } from "../../types/api";

interface ProduitFormModalProps {
  open: boolean;
  produit: Produit | null; // null = creation, sinon modification
  onClose: () => void;
}

export function ProduitFormModal({ open, produit, onClose }: ProduitFormModalProps) {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();
  const isEdit = produit !== null;

  const {
    register,
    control,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting }
  } = useForm<ProduitFormValues>({
    resolver: zodResolver(produitSchema),
    defaultValues: {
      nomCommercial: "",
      composition: "",
      formPharmaceutique: "",
      dosage: "",
      prixP: 0,
      quantiteStock: 0,
      datePeremption: ""
    }
  });

  useEffect(() => {
    if (open) {
      reset(
        produit
          ? {
              nomCommercial: produit.nomCommercial,
              composition: produit.composition ?? "",
              formPharmaceutique: produit.formPharmaceutique ?? "",
              dosage: produit.dosage ?? "",
              prixP: produit.prixP,
              quantiteStock: produit.quantiteStock,
              datePeremption: produit.datePeremption ?? ""
            }
          : {
              nomCommercial: "",
              composition: "",
              formPharmaceutique: "",
              dosage: "",
              prixP: 0,
              quantiteStock: 0,
              datePeremption: ""
            }
      );
    }
  }, [open, produit, reset]);

  const mutation = useMutation({
    mutationFn: (values: ProduitFormValues) => {
      const payload = {
        nomCommercial: values.nomCommercial,
        composition: values.composition ?? null,
        formPharmaceutique: values.formPharmaceutique ?? null,
        dosage: values.dosage ?? null,
        prixP: values.prixP,
        quantiteStock: values.quantiteStock,
        datePeremption: values.datePeremption
      };
      return isEdit
        ? produitsApi.updateProduit(produit.idProduit, payload)
        : produitsApi.createProduit(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["produits"] });
      showSuccess(isEdit ? "Produit modifié avec succès." : "Produit ajouté avec succès.");
      onClose();
    },
    onError: (err) => {
      showError(err instanceof ApiError ? err.message : "Une erreur est survenue.");
    }
  });

  const nomValue = watch("nomCommercial");

  return (
    <Modal open={open} title={isEdit ? "Modifier Produit" : "Ajouter Produit"} onClose={onClose}>
      <form
        onSubmit={handleSubmit((values) => mutation.mutate(values))}
        noValidate
        className="grid grid-cols-1 gap-x-4 sm:grid-cols-2"
      >
        <FormField
          label="Nom:"
          htmlFor="produit-nom"
          error={errors.nomCommercial?.message}
          required
        >
          <input
            id="produit-nom"
            type="text"
            className={inputClassName}
            {...register("nomCommercial")}
          />
        </FormField>

        <FormField
          label="Description:"
          htmlFor="produit-description"
          error={errors.composition?.message}
        >
          <input
            id="produit-description"
            type="text"
            placeholder="Description"
            className={inputClassName}
            {...register("composition")}
          />
        </FormField>

        <FormField label="Prix:" htmlFor="produit-prix" error={errors.prixP?.message} required>
          <input
            id="produit-prix"
            type="number"
            step="0.01"
            min="0"
            placeholder="Prix"
            className={inputClassName}
            {...register("prixP")}
          />
        </FormField>

        <FormField
          label="Stock:"
          htmlFor="produit-stock"
          error={errors.quantiteStock?.message}
          required
        >
          <input
            id="produit-stock"
            type="number"
            step="1"
            min="0"
            placeholder="Stock"
            className={inputClassName}
            {...register("quantiteStock")}
          />
        </FormField>

        <FormField
          label="Date péremption:"
          htmlFor="produit-date-peremption"
          error={errors.datePeremption?.message}
          required
        >
          <Controller
            control={control}
            name="datePeremption"
            render={({ field }) => (
              <DatePickerField
                id="produit-date-peremption"
                value={field.value}
                onChange={field.onChange}
              />
            )}
          />
        </FormField>

        {/* Champs presents dans le vrai ProduitDTO backend mais non decrits dans la
            capture video : ajoutes en optionnel pour ne pas perdre de donnees reelles. */}
        <FormField
          label="Forme pharmaceutique:"
          htmlFor="produit-forme"
          error={errors.formPharmaceutique?.message}
        >
          <input
            id="produit-forme"
            type="text"
            placeholder="ex: Comprimé, Sirop"
            className={inputClassName}
            {...register("formPharmaceutique")}
          />
        </FormField>

        <FormField label="Dosage:" htmlFor="produit-dosage" error={errors.dosage?.message}>
          <input
            id="produit-dosage"
            type="text"
            placeholder="ex: 500mg"
            className={inputClassName}
            {...register("dosage")}
          />
        </FormField>

        <div className="col-span-full mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={!nomValue || nomValue.trim() === "" || isSubmitting}
          >
            Valider
          </Button>
        </div>
      </form>
    </Modal>
  );
}

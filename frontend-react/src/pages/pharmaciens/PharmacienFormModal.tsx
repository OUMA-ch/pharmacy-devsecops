import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import * as pharmaciensApi from "../../api/pharmaciens";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { Modal } from "../../components/ui/Modal";
import { Button } from "../../components/ui/Button";
import { FormField, inputClassName } from "../../components/ui/FormField";
import { pharmacienCreateSchema, type PharmacienFormValues } from "../../lib/validationSchemas";
import type { Pharmacien } from "../../types/api";

interface PharmacienFormModalProps {
  open: boolean;
  pharmacien: Pharmacien | null;
  onClose: () => void;
}

/**
 * Ni cette modale ni l'API (PharmacienCrudService) ne proposent de choix de role :
 * un compte cree ici est toujours PHARMACIEN. Aucun endpoint ne permet de creer un
 * compte RESPONSABLE (verifie dans le backend).
 */
export function PharmacienFormModal({ open, pharmacien, onClose }: PharmacienFormModalProps) {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();
  const isEdit = pharmacien !== null;

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting }
  } = useForm<PharmacienFormValues>({
    resolver: zodResolver(
      isEdit ? pharmacienCreateSchema.partial({ password: true }) : pharmacienCreateSchema
    ),
    defaultValues: { nomUser: "", email: "", password: "", tele: "" }
  });

  useEffect(() => {
    if (open) {
      reset(
        pharmacien
          ? {
              nomUser: pharmacien.nomUser,
              email: pharmacien.email,
              password: "",
              tele: pharmacien.tele ?? ""
            }
          : { nomUser: "", email: "", password: "", tele: "" }
      );
    }
  }, [open, pharmacien, reset]);

  const mutation = useMutation({
    mutationFn: (values: PharmacienFormValues) =>
      isEdit
        ? pharmaciensApi.updatePharmacien(pharmacien.idUser, {
            nomUser: values.nomUser,
            email: values.email,
            tele: values.tele,
            ...(values.password ? { password: values.password } : {})
          })
        : pharmaciensApi.createPharmacien({ ...values, role: "PHARMACIEN" }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pharmaciens"] });
      showSuccess(isEdit ? "Pharmacien modifié." : "Pharmacien créé.");
      onClose();
    },
    onError: (err) => showError(err instanceof ApiError ? err.message : "Une erreur est survenue.")
  });

  const nom = watch("nomUser");
  const email = watch("email");

  return (
    <Modal
      open={open}
      title={isEdit ? "Modifier Pharmacien" : "Ajouter Pharmacien"}
      onClose={onClose}
    >
      <form onSubmit={handleSubmit((v) => mutation.mutate(v))} noValidate>
        <FormField label="Nom:" htmlFor="pharmacien-nom" error={errors.nomUser?.message} required>
          <input
            id="pharmacien-nom"
            type="text"
            className={inputClassName}
            {...register("nomUser")}
          />
        </FormField>
        <FormField label="Email:" htmlFor="pharmacien-email" error={errors.email?.message} required>
          <input
            id="pharmacien-email"
            type="email"
            className={inputClassName}
            {...register("email")}
          />
        </FormField>
        <FormField
          label="Password:"
          htmlFor="pharmacien-password"
          error={errors.password?.message}
          required={!isEdit}
        >
          <input
            id="pharmacien-password"
            type="password"
            placeholder={isEdit ? "Laisser vide pour ne pas changer" : undefined}
            className={inputClassName}
            {...register("password")}
          />
        </FormField>
        <FormField label="Téléphone:" htmlFor="pharmacien-tele" error={errors.tele?.message}>
          <input id="pharmacien-tele" type="tel" className={inputClassName} {...register("tele")} />
        </FormField>
        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" disabled={!nom || !email || isSubmitting}>
            Créer
          </Button>
        </div>
      </form>
    </Modal>
  );
}

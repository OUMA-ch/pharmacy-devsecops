import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { Modal } from "../../components/ui/Modal";
import { Button } from "../../components/ui/Button";
import { FormField, inputClassName } from "../../components/ui/FormField";
import { DatePickerField } from "../../components/ui/DatePickerField";
import { ordonnanceSchema, type OrdonnanceFormValues } from "../../lib/validationSchemas";

interface OrdonnanceFormModalProps {
  open: boolean;
  onCancel: () => void;
  onSubmit: (values: OrdonnanceFormValues) => void;
  isSubmitting?: boolean;
}

/**
 * S'ouvre automatiquement depuis VenteFormModal quand "Avec ordonnance ?" est
 * coche et que "Valider" est clique sur la vente.
 */
export function OrdonnanceFormModal({
  open,
  onCancel,
  onSubmit,
  isSubmitting
}: OrdonnanceFormModalProps) {
  const {
    register,
    control,
    handleSubmit,
    reset,
    watch,
    formState: { errors }
  } = useForm<OrdonnanceFormValues>({
    resolver: zodResolver(ordonnanceSchema),
    defaultValues: { nomMedecin: "", dateEmission: "", description: "" }
  });

  useEffect(() => {
    if (open) reset({ nomMedecin: "", dateEmission: "", description: "" });
  }, [open, reset]);

  const nomMedecin = watch("nomMedecin");

  return (
    <Modal open={open} title="Nouvelle Ordonnance" onClose={onCancel}>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <FormField
          label="Médecin:"
          htmlFor="ordonnance-medecin"
          error={errors.nomMedecin?.message}
          required
        >
          <input
            id="ordonnance-medecin"
            type="text"
            placeholder="Nom du médecin"
            className={inputClassName}
            {...register("nomMedecin")}
          />
        </FormField>

        <FormField
          label="Date émission:"
          htmlFor="ordonnance-date"
          error={errors.dateEmission?.message}
          required
        >
          <Controller
            control={control}
            name="dateEmission"
            render={({ field }) => (
              <DatePickerField id="ordonnance-date" value={field.value} onChange={field.onChange} />
            )}
          />
        </FormField>

        <FormField
          label="Description:"
          htmlFor="ordonnance-description"
          error={errors.description?.message}
        >
          <textarea
            id="ordonnance-description"
            rows={4}
            placeholder="Description"
            className={inputClassName}
            {...register("description")}
          />
        </FormField>

        <div className="mt-2 flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={!nomMedecin || nomMedecin.trim() === "" || isSubmitting}
          >
            Valider
          </Button>
        </div>
      </form>
    </Modal>
  );
}

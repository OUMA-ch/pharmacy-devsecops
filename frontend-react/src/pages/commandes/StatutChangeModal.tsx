import { useEffect, useState } from "react";
import { Modal } from "../../components/ui/Modal";
import { Button } from "../../components/ui/Button";
import { STATUTS_COMMANDE } from "../../lib/constants";
import type { StatutCommande } from "../../types/api";

interface StatutChangeModalProps {
  open: boolean;
  currentStatut: string;
  onCancel: () => void;
  onConfirm: (statut: StatutCommande) => void;
  isSubmitting?: boolean;
}

export function StatutChangeModal({
  open,
  currentStatut,
  onCancel,
  onConfirm,
  isSubmitting
}: StatutChangeModalProps) {
  const [statut, setStatut] = useState<StatutCommande>("EN_ATTENTE");

  useEffect(() => {
    if (open && STATUTS_COMMANDE.includes(currentStatut as StatutCommande)) {
      setStatut(currentStatut as StatutCommande);
    }
  }, [open, currentStatut]);

  return (
    <Modal
      open={open}
      title="Changer le statut"
      onClose={onCancel}
      footer={
        <>
          <Button variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
          <Button variant="primary" onClick={() => onConfirm(statut)} disabled={isSubmitting}>
            Valider
          </Button>
        </>
      }
    >
      <label htmlFor="statut-select" className="mb-1 block text-sm font-medium text-slate-700">
        Statut:
      </label>
      <select
        id="statut-select"
        value={statut}
        onChange={(e) => setStatut(e.target.value as StatutCommande)}
        className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
      >
        {STATUTS_COMMANDE.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>
    </Modal>
  );
}

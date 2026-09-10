import { Modal } from "./Modal";
import { Button } from "./Button";

interface ConfirmDialogProps {
  open: boolean;
  title?: string;
  message: string;
  confirmLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

/**
 * Remplace tout window.confirm() natif : utilisee avant chaque action "Supprimer"
 * (produit, vente, commande, fournisseur, fourniture, pharmacien).
 */
export function ConfirmDialog({
  open,
  title = "Confirmer la suppression",
  message,
  confirmLabel = "Supprimer",
  onConfirm,
  onCancel
}: ConfirmDialogProps) {
  return (
    <Modal
      open={open}
      title={title}
      icon={<span aria-hidden>⚠️</span>}
      onClose={onCancel}
      footer={
        <>
          <Button variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
          <Button variant="danger" onClick={onConfirm}>
            {confirmLabel}
          </Button>
        </>
      }
    >
      <p className="text-sm text-slate-600">{message}</p>
    </Modal>
  );
}

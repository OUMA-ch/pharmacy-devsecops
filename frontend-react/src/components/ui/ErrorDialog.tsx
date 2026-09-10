import { Modal } from "./Modal";
import { Button } from "./Button";

interface ErrorDialogProps {
  open: boolean;
  message: string;
  onClose: () => void;
}

/**
 * Modale d'erreur de validation, ex. commande sans produit/quantite valide :
 * icone ❌ rouge, titre "Erreur", message exact, bouton "OK".
 */
export function ErrorDialog({ open, message, onClose }: ErrorDialogProps) {
  return (
    <Modal
      open={open}
      title="Erreur"
      icon={
        <span aria-hidden className="text-red-600">
          ❌
        </span>
      }
      onClose={onClose}
      footer={
        <Button variant="primary" onClick={onClose}>
          OK
        </Button>
      }
    >
      <p className="text-sm text-slate-600">{message}</p>
    </Modal>
  );
}

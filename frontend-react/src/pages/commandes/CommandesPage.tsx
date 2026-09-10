import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as commandesApi from "../../api/commandes";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { PageHeader } from "../../components/layout/PageHeader";
import { Button } from "../../components/ui/Button";
import { Badge } from "../../components/ui/Badge";
import { DataTable, type Column } from "../../components/ui/DataTable";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import { formatDate } from "../../lib/formatters";
import { STATUT_BADGE_TONE, STATUTS_COMMANDE } from "../../lib/constants";
import type { CommandeDTO, LigneCommandeDTO, StatutCommande } from "../../types/api";
import { CommandeFormModal } from "./CommandeFormModal";
import { StatutChangeModal } from "./StatutChangeModal";

function badgeToneFor(statut: string) {
  return STATUTS_COMMANDE.includes(statut as StatutCommande)
    ? STATUT_BADGE_TONE[statut as StatutCommande]
    : "gray";
}

export function CommandesPage() {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedCommande, setSelectedCommande] = useState<CommandeDTO | null>(null);
  const [statutTarget, setStatutTarget] = useState<CommandeDTO | null>(null);
  const [deletingCommande, setDeletingCommande] = useState<CommandeDTO | null>(null);

  const commandesQuery = useQuery({ queryKey: ["commandes"], queryFn: commandesApi.listCommandes });

  const statutMutation = useMutation({
    mutationFn: ({ numCmd, statut }: { numCmd: number; statut: StatutCommande }) =>
      commandesApi.changerStatutCommande(numCmd, statut),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["commandes"] });
      showSuccess("Statut de la commande mis à jour.");
      setStatutTarget(null);
    },
    onError: (err) => {
      showError(err instanceof ApiError ? err.message : "Impossible de changer le statut.");
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (numCmd: number) => commandesApi.deleteCommande(numCmd),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["commandes"] });
      showSuccess("Commande supprimée.");
      setDeletingCommande(null);
      setSelectedCommande((current) =>
        current?.idCommande === deletingCommande?.idCommande ? null : current
      );
    },
    onError: (err) => {
      showError(err instanceof ApiError ? err.message : "Suppression impossible.");
      setDeletingCommande(null);
    }
  });

  const columns: Column<CommandeDTO>[] = [
    { key: "id", header: "Num Cmd", render: (c) => c.idCommande },
    { key: "date", header: "Date", render: (c) => formatDate(c.dateCommande) },
    {
      key: "statut",
      header: "Statut",
      render: (c) => <Badge tone={badgeToneFor(c.statut)}>{c.statut}</Badge>
    },
    { key: "fournisseurId", header: "Fournisseur ID", render: (c) => c.fournisseurId ?? "-" },
    {
      key: "actions",
      header: "Actions",
      render: (c) => (
        <div className="flex gap-2">
          <Button
            variant="secondary"
            onClick={(e) => {
              e.stopPropagation();
              setStatutTarget(c);
            }}
          >
            Statut
          </Button>
          <Button
            variant="danger"
            onClick={(e) => {
              e.stopPropagation();
              setDeletingCommande(c);
            }}
          >
            Supprimer
          </Button>
        </div>
      )
    }
  ];

  const ligneColumns: Column<LigneCommandeDTO>[] = [
    { key: "produitId", header: "Produit ID", render: (l) => l.produitId },
    { key: "quantite", header: "Quantité demandée", render: (l) => l.quantiteDemande }
  ];

  return (
    <div>
      <PageHeader title="Gestion des Commandes" />

      <div className="mb-4 flex justify-end gap-2">
        <Button variant="secondary" onClick={() => commandesQuery.refetch()}>
          Afficher
        </Button>
        <Button variant="success" onClick={() => setModalOpen(true)}>
          Nouvelle commande
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={commandesQuery.data}
        isLoading={commandesQuery.isLoading}
        getRowId={(c) => c.idCommande}
        selectedRowId={selectedCommande?.idCommande ?? null}
        onRowClick={setSelectedCommande}
      />

      <h2 className="mb-2 mt-6 text-sm font-semibold text-slate-700">Lignes de commande</h2>
      <DataTable
        columns={ligneColumns}
        data={selectedCommande?.lignes ?? []}
        getRowId={(l) => `${l.produitId}`}
        emptyMessage={
          selectedCommande ? "Aucune donnée" : "Sélectionnez une commande pour voir ses lignes"
        }
      />

      <CommandeFormModal open={modalOpen} onClose={() => setModalOpen(false)} />

      <StatutChangeModal
        open={statutTarget !== null}
        currentStatut={statutTarget?.statut ?? "EN_ATTENTE"}
        onCancel={() => setStatutTarget(null)}
        isSubmitting={statutMutation.isPending}
        onConfirm={(statut) =>
          statutTarget && statutMutation.mutate({ numCmd: statutTarget.idCommande, statut })
        }
      />

      <ConfirmDialog
        open={deletingCommande !== null}
        message={`Supprimer la commande #${deletingCommande?.idCommande} ?`}
        onCancel={() => setDeletingCommande(null)}
        onConfirm={() => deletingCommande && deleteMutation.mutate(deletingCommande.idCommande)}
      />
    </div>
  );
}

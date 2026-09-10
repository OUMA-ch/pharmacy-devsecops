import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as produitsApi from "../../api/produits";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { PageHeader } from "../../components/layout/PageHeader";
import { Button } from "../../components/ui/Button";
import { DataTable, type Column } from "../../components/ui/DataTable";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import { formatDate, formatCurrencyDH } from "../../lib/formatters";
import type { Produit } from "../../types/api";
import { ProduitFormModal } from "./ProduitFormModal";

export function ProduitsPage() {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();

  const [modalOpen, setModalOpen] = useState(false);
  const [editingProduit, setEditingProduit] = useState<Produit | null>(null);
  const [deletingProduit, setDeletingProduit] = useState<Produit | null>(null);

  const produitsQuery = useQuery({ queryKey: ["produits"], queryFn: produitsApi.listProduits });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => produitsApi.deleteProduit(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["produits"] });
      showSuccess("Produit supprimé.");
      setDeletingProduit(null);
    },
    onError: (err) => {
      showError(err instanceof ApiError ? err.message : "Suppression impossible.");
      setDeletingProduit(null);
    }
  });

  const columns: Column<Produit>[] = [
    { key: "id", header: "ID", render: (p) => p.idProduit },
    { key: "nom", header: "Nom", render: (p) => p.nomCommercial },
    { key: "description", header: "Description", render: (p) => p.composition ?? "-" },
    { key: "prix", header: "Prix", render: (p) => formatCurrencyDH(p.prixP) },
    { key: "peremption", header: "Date péremption", render: (p) => formatDate(p.datePeremption) },
    { key: "stock", header: "Stock", render: (p) => p.quantiteStock },
    {
      key: "actions",
      header: "Actions",
      render: (p) => (
        <div className="flex gap-2">
          <Button
            variant="secondary"
            onClick={(e) => {
              e.stopPropagation();
              setEditingProduit(p);
              setModalOpen(true);
            }}
          >
            Modifier
          </Button>
          <Button
            variant="danger"
            onClick={(e) => {
              e.stopPropagation();
              setDeletingProduit(p);
            }}
          >
            Supprimer
          </Button>
        </div>
      )
    }
  ];

  return (
    <div>
      <PageHeader title="Gestion des Produits" />

      <div className="mb-4 flex justify-end gap-2">
        <Button variant="secondary" onClick={() => produitsQuery.refetch()}>
          Actualiser
        </Button>
        <Button
          variant="success"
          onClick={() => {
            setEditingProduit(null);
            setModalOpen(true);
          }}
        >
          Ajouter
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={produitsQuery.data}
        isLoading={produitsQuery.isLoading}
        getRowId={(p) => p.idProduit}
      />

      <ProduitFormModal
        open={modalOpen}
        produit={editingProduit}
        onClose={() => setModalOpen(false)}
      />

      <ConfirmDialog
        open={deletingProduit !== null}
        message={`Supprimer le produit "${deletingProduit?.nomCommercial}" ?`}
        onCancel={() => setDeletingProduit(null)}
        onConfirm={() => deletingProduit && deleteMutation.mutate(deletingProduit.idProduit)}
      />
    </div>
  );
}

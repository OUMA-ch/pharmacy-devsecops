import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as fournisseursApi from "../../api/fournisseurs";
import * as fournituresApi from "../../api/fournitures";
import { ApiError } from "../../api/client";
import { useToast } from "../../components/ui/ToastProvider";
import { PageHeader } from "../../components/layout/PageHeader";
import { Button } from "../../components/ui/Button";
import { DataTable, type Column } from "../../components/ui/DataTable";
import { ConfirmDialog } from "../../components/ui/ConfirmDialog";
import { formatCurrencyDH } from "../../lib/formatters";
import type { Fournisseur, FournitureDTO } from "../../types/api";
import { FournisseurFormModal } from "./FournisseurFormModal";
import { FournitureFormModal } from "./FournitureFormModal";
import { ModifierPrixModal } from "./ModifierPrixModal";

export function FournisseursPage() {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();

  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<Fournisseur | null>(null);
  const [fournisseurModalOpen, setFournisseurModalOpen] = useState(false);
  const [editingFournisseur, setEditingFournisseur] = useState<Fournisseur | null>(null);
  const [deletingFournisseur, setDeletingFournisseur] = useState<Fournisseur | null>(null);
  const [fournitureModalOpen, setFournitureModalOpen] = useState(false);
  const [prixTarget, setPrixTarget] = useState<FournitureDTO | null>(null);
  const [deletingFourniture, setDeletingFourniture] = useState<FournitureDTO | null>(null);

  const fournisseursQuery = useQuery({
    queryKey: ["fournisseurs"],
    queryFn: fournisseursApi.listFournisseurs
  });

  const fournituresQuery = useQuery({
    queryKey: ["fournitures", selected?.idFournisseur],
    queryFn: () => fournituresApi.listFournituresByFournisseur(selected!.idFournisseur),
    enabled: selected !== null
  });

  const deleteFournisseurMutation = useMutation({
    mutationFn: (id: number) => fournisseursApi.deleteFournisseur(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fournisseurs"] });
      showSuccess("Fournisseur supprimé.");
      setDeletingFournisseur(null);
      setSelected((current) =>
        current?.idFournisseur === deletingFournisseur?.idFournisseur ? null : current
      );
    },
    onError: (err) => {
      showError(err instanceof ApiError ? err.message : "Suppression impossible.");
      setDeletingFournisseur(null);
    }
  });

  const deleteFournitureMutation = useMutation({
    mutationFn: (produitId: number) =>
      fournituresApi.supprimerFourniture(produitId, selected!.idFournisseur),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["fournitures", selected?.idFournisseur] });
      showSuccess("Fourniture supprimée.");
      setDeletingFourniture(null);
    },
    onError: (err) => {
      showError(err instanceof ApiError ? err.message : "Suppression impossible.");
      setDeletingFourniture(null);
    }
  });

  const filteredFournisseurs = useMemo(() => {
    const list = fournisseursQuery.data ?? [];
    const term = search.trim().toLowerCase();
    if (!term) return list;
    return list.filter(
      (f) =>
        f.nomFournisseur.toLowerCase().includes(term) || (f.tel ?? "").toLowerCase().includes(term)
    );
  }, [fournisseursQuery.data, search]);

  const fournisseurColumns: Column<Fournisseur>[] = [
    { key: "id", header: "ID", render: (f) => f.idFournisseur },
    { key: "nom", header: "Nom", render: (f) => f.nomFournisseur },
    { key: "tel", header: "Téléphone", render: (f) => f.tel ?? "-" },
    {
      key: "actions",
      header: "Actions",
      render: (f) => (
        <div className="flex gap-2">
          <Button
            variant="secondary"
            onClick={(e) => {
              e.stopPropagation();
              setEditingFournisseur(f);
              setFournisseurModalOpen(true);
            }}
          >
            Modifier
          </Button>
          <Button
            variant="danger"
            onClick={(e) => {
              e.stopPropagation();
              setDeletingFournisseur(f);
            }}
          >
            Supprimer
          </Button>
        </div>
      )
    }
  ];

  const fournitureColumns: Column<FournitureDTO>[] = [
    { key: "produitId", header: "Produit ID", render: (f) => f.idProduit },
    { key: "nomProduit", header: "Nom Produit", render: (f) => f.nomProduit ?? "-" },
    { key: "prixAchat", header: "Prix Achat", render: (f) => formatCurrencyDH(f.prixAchat) },
    {
      key: "actions",
      header: "Actions",
      render: (f) => (
        <div className="flex gap-2">
          <Button variant="secondary" onClick={() => setPrixTarget(f)}>
            Prix
          </Button>
          <Button variant="danger" onClick={() => setDeletingFourniture(f)}>
            Supprimer
          </Button>
        </div>
      )
    }
  ];

  return (
    <div>
      <PageHeader title="Gestion des Fournisseurs" />

      <div className="mb-4 flex flex-wrap items-center justify-end gap-2">
        <input
          type="text"
          placeholder="Rechercher (nom / tel)"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="min-w-[220px] rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
        <Button variant="secondary" onClick={() => fournisseursQuery.refetch()}>
          Actualiser
        </Button>
        <Button
          variant="success"
          onClick={() => {
            setEditingFournisseur(null);
            setFournisseurModalOpen(true);
          }}
        >
          Ajouter fournisseur
        </Button>
      </div>

      <div className="max-h-[360px] overflow-y-auto">
        <DataTable
          columns={fournisseurColumns}
          data={filteredFournisseurs}
          isLoading={fournisseursQuery.isLoading}
          getRowId={(f) => f.idFournisseur}
          selectedRowId={selected?.idFournisseur ?? null}
          onRowClick={setSelected}
        />
      </div>

      <div className="mb-3 mt-6 flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-sm font-semibold text-slate-700">
          Fournitures du fournisseur sélectionné
        </h2>
        <div className="flex gap-2">
          <Button
            variant="secondary"
            onClick={() => fournituresQuery.refetch()}
            disabled={!selected}
          >
            Actualiser fournitures
          </Button>
          <Button
            variant="primary"
            onClick={() => setFournitureModalOpen(true)}
            disabled={!selected}
          >
            Ajouter produit (fourniture)
          </Button>
        </div>
      </div>

      <DataTable
        columns={fournitureColumns}
        data={fournituresQuery.data}
        isLoading={selected !== null && fournituresQuery.isLoading}
        getRowId={(f) => f.idProduit}
        emptyMessage={
          selected ? "Aucune donnée" : "Sélectionnez un fournisseur pour voir ses fournitures"
        }
      />

      <FournisseurFormModal
        open={fournisseurModalOpen}
        fournisseur={editingFournisseur}
        onClose={() => setFournisseurModalOpen(false)}
      />

      {selected && (
        <FournitureFormModal
          open={fournitureModalOpen}
          idFournisseur={selected.idFournisseur}
          onClose={() => setFournitureModalOpen(false)}
        />
      )}

      {selected && (
        <ModifierPrixModal
          open={prixTarget !== null}
          fourniture={prixTarget}
          idFournisseur={selected.idFournisseur}
          onClose={() => setPrixTarget(null)}
        />
      )}

      <ConfirmDialog
        open={deletingFournisseur !== null}
        message={`Supprimer le fournisseur "${deletingFournisseur?.nomFournisseur}" ?`}
        onCancel={() => setDeletingFournisseur(null)}
        onConfirm={() =>
          deletingFournisseur && deleteFournisseurMutation.mutate(deletingFournisseur.idFournisseur)
        }
      />

      <ConfirmDialog
        open={deletingFourniture !== null}
        message={`Retirer le produit #${deletingFourniture?.idProduit} des fournitures de ce fournisseur ?`}
        onCancel={() => setDeletingFourniture(null)}
        onConfirm={() =>
          deletingFourniture && deleteFournitureMutation.mutate(deletingFourniture.idProduit)
        }
      />
    </div>
  );
}

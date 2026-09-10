import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import * as ventesApi from "../../api/ventes";
import { PageHeader } from "../../components/layout/PageHeader";
import { Button } from "../../components/ui/Button";
import { DataTable, type Column } from "../../components/ui/DataTable";
import { formatDate, formatCurrencyDH } from "../../lib/formatters";
import type { VenteDTO } from "../../types/api";
import { VenteFormModal } from "./VenteFormModal";

export function VentesPage() {
  const [clientIdFilter, setClientIdFilter] = useState("");
  const [appliedClientId, setAppliedClientId] = useState<number | null>(null);
  const [modalOpen, setModalOpen] = useState(false);

  const ventesQuery = useQuery({
    queryKey: ["ventes", appliedClientId],
    queryFn: () =>
      appliedClientId ? ventesApi.listVentesByClient(appliedClientId) : ventesApi.listVentes()
  });

  function handleAfficher() {
    const parsed = Number(clientIdFilter);
    setAppliedClientId(clientIdFilter.trim() && Number.isFinite(parsed) ? parsed : null);
  }

  const columns: Column<VenteDTO>[] = [
    { key: "id", header: "ID", render: (v) => v.idVente },
    { key: "date", header: "Date", render: (v) => formatDate(v.dateVente) },
    { key: "produitId", header: "Produit ID", render: (v) => v.produitId },
    { key: "quantite", header: "Quantité", render: (v) => v.quantite },
    { key: "prixTotal", header: "Prix Total", render: (v) => formatCurrencyDH(v.prixTotal) },
    { key: "ordonnanceId", header: "Ordonnance ID", render: (v) => v.ordonnanceId ?? "-" },
    { key: "medecin", header: "Médecin", render: (v) => v.ordonnanceNomMedecin ?? "-" },
    {
      key: "dateOrdonnance",
      header: "Date ordonnance",
      render: (v) => formatDate(v.ordonnanceDateEmission)
    }
  ];

  return (
    <div>
      <PageHeader title="Gestion des Ventes" />

      <div className="mb-4 flex flex-wrap items-center justify-end gap-2">
        <label htmlFor="ventes-client-filter" className="text-sm font-medium text-slate-700">
          Client ID:
        </label>
        <input
          id="ventes-client-filter"
          type="number"
          min={1}
          placeholder="ex: 1"
          value={clientIdFilter}
          onChange={(e) => setClientIdFilter(e.target.value)}
          className="w-24 rounded-md border border-slate-300 px-2 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
        <Button variant="secondary" onClick={handleAfficher}>
          Afficher
        </Button>
        <Button variant="success" onClick={() => setModalOpen(true)}>
          Nouvelle vente
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={ventesQuery.data}
        isLoading={ventesQuery.isLoading}
        getRowId={(v) => v.idVente}
      />

      <VenteFormModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  );
}

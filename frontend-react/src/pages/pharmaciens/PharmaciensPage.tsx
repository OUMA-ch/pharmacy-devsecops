import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import * as pharmaciensApi from "../../api/pharmaciens";
import { PageHeader } from "../../components/layout/PageHeader";
import { Button } from "../../components/ui/Button";
import { DataTable, type Column } from "../../components/ui/DataTable";
import type { Pharmacien } from "../../types/api";
import { PharmacienFormModal } from "./PharmacienFormModal";

export function PharmaciensPage() {
  const [modalOpen, setModalOpen] = useState(false);
  const [editingPharmacien, setEditingPharmacien] = useState<Pharmacien | null>(null);

  const pharmaciensQuery = useQuery({
    queryKey: ["pharmaciens"],
    queryFn: pharmaciensApi.listPharmaciens
  });

  const columns: Column<Pharmacien>[] = [
    { key: "id", header: "ID", render: (p) => p.idUser },
    { key: "nom", header: "Nom", render: (p) => p.nomUser },
    { key: "email", header: "Email", render: (p) => p.email },
    { key: "tele", header: "Téléphone", render: (p) => p.tele ?? "-" },
    { key: "role", header: "Role", render: (p) => p.role.toUpperCase() },
    {
      key: "actions",
      header: "Actions",
      render: (p) => (
        <Button
          variant="secondary"
          onClick={() => {
            setEditingPharmacien(p);
            setModalOpen(true);
          }}
        >
          Modifier
        </Button>
      )
    }
  ];

  return (
    <div>
      <PageHeader title="Gestion des Pharmaciens" />

      <div className="mb-4 flex justify-end gap-2">
        <Button variant="secondary" onClick={() => pharmaciensQuery.refetch()}>
          Actualiser
        </Button>
        <Button
          variant="success"
          onClick={() => {
            setEditingPharmacien(null);
            setModalOpen(true);
          }}
        >
          Ajouter
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={pharmaciensQuery.data}
        isLoading={pharmaciensQuery.isLoading}
        getRowId={(p) => p.idUser}
      />

      <PharmacienFormModal
        open={modalOpen}
        pharmacien={editingPharmacien}
        onClose={() => setModalOpen(false)}
      />
    </div>
  );
}

import type { ReactNode } from "react";
import clsx from "clsx";
import { Spinner } from "./Spinner";

export interface Column<T> {
  key: string;
  header: string;
  render: (row: T) => ReactNode;
  className?: string;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[] | undefined;
  isLoading?: boolean;
  emptyMessage?: string;
  getRowId: (row: T) => string | number;
  selectedRowId?: string | number | null;
  onRowClick?: (row: T) => void;
}

/**
 * Tableau generique reutilise par tous les modules (Produits, Ventes, Commandes,
 * Fournisseurs, Fournitures, Pharmaciens, lignes de commande...). Supporte le
 * scroll horizontal quand les colonnes ne rentrent pas (comportement observe dans
 * l'app d'origine), un etat de chargement, un etat vide explicite, et la selection
 * de ligne (surlignage gris) utilisee par Commandes/Fournisseurs.
 */
export function DataTable<T>({
  columns,
  data,
  isLoading,
  emptyMessage = "Aucune donnée",
  getRowId,
  selectedRowId,
  onRowClick
}: DataTableProps<T>) {
  return (
    <div className="overflow-x-auto rounded-md border border-slate-200">
      <table className="w-full min-w-max text-left text-sm">
        <thead className="bg-slate-100">
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                className="whitespace-nowrap px-4 py-2 font-semibold text-slate-700"
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {isLoading ? (
            <tr>
              <td colSpan={columns.length}>
                <Spinner />
              </td>
            </tr>
          ) : !data || data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="px-4 py-8 text-center text-slate-400">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row) => {
              const id = getRowId(row);
              const isSelected = selectedRowId !== undefined && selectedRowId === id;
              return (
                <tr
                  key={id}
                  onClick={onRowClick ? () => onRowClick(row) : undefined}
                  className={clsx(
                    "border-t border-slate-100",
                    onRowClick && "cursor-pointer hover:bg-slate-50",
                    isSelected && "bg-slate-200 hover:bg-slate-200"
                  )}
                >
                  {columns.map((col) => (
                    <td
                      key={col.key}
                      className={clsx("whitespace-nowrap px-4 py-2", col.className)}
                    >
                      {col.render(row)}
                    </td>
                  ))}
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}

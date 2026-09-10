import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../auth/AuthContext";
import * as notificationsApi from "../../api/notifications";
import { PageHeader } from "../../components/layout/PageHeader";
import { Button } from "../../components/ui/Button";
import { Badge } from "../../components/ui/Badge";
import { Spinner } from "../../components/ui/Spinner";
import { formatDateTime } from "../../lib/formatters";
import type { Notification } from "../../types/api";

export function ClientNotificationsPage() {
  const { user } = useAuth();
  const clientId = user!.id;
  const queryClient = useQueryClient();

  const [queryInput, setQueryInput] = useState("");
  const [submittedQuery, setSubmittedQuery] = useState("");
  const [unreadOnly, setUnreadOnly] = useState(false);
  // Etat local optimiste : le backend n'expose aucune route pour marquer une
  // notification comme lue (NotificationController n'a que des GET). Ce Set
  // simule visuellement la lecture mais n'est PAS persiste : il revient a l'etat
  // serveur au prochain rechargement/refetch (voir README, section limites connues).
  const [locallyRead, setLocallyRead] = useState<Set<number>>(new Set());

  const baseQuery = useQuery({
    queryKey: ["notifications", clientId],
    queryFn: () => notificationsApi.listNotifications(clientId)
  });

  const searchQuery = useQuery({
    queryKey: ["notifications-search", clientId, submittedQuery],
    queryFn: () => notificationsApi.searchNotifications(clientId, submittedQuery),
    enabled: submittedQuery.trim().length > 0
  });

  const baseList = baseQuery.data ?? [];
  const isSearching = submittedQuery.trim().length > 0;
  const sourceList = isSearching ? (searchQuery.data ?? []) : baseList;
  const isLoading = isSearching ? searchQuery.isLoading : baseQuery.isLoading;

  function isRead(n: Notification): boolean {
    return n.lu || locallyRead.has(n.id);
  }

  const total = baseList.length;
  const unreadCount = baseList.filter((n) => !isRead(n)).length;
  const displayed = sourceList.filter((n) => (unreadOnly ? !isRead(n) : true));

  function handleSearchSubmit(event: React.FormEvent) {
    event.preventDefault();
    setSubmittedQuery(queryInput);
  }

  function handleRefresh() {
    queryClient.invalidateQueries({ queryKey: ["notifications", clientId] });
    if (isSearching) {
      queryClient.invalidateQueries({
        queryKey: ["notifications-search", clientId, submittedQuery]
      });
    }
  }

  function handleCardClick(n: Notification) {
    if (!isRead(n)) {
      setLocallyRead((prev) => new Set(prev).add(n.id));
    }
  }

  return (
    <div>
      <PageHeader title="Notifications" />

      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-slate-500">
          Total: {total} • Non lus: {unreadCount}
        </p>
        <Button variant="secondary" onClick={handleRefresh}>
          Rafraîchir
        </Button>
      </div>

      <form onSubmit={handleSearchSubmit} className="mb-6 flex flex-wrap items-center gap-3">
        <input
          type="text"
          placeholder="Rechercher (titre ou message)..."
          value={queryInput}
          onChange={(e) => setQueryInput(e.target.value)}
          className="min-w-[260px] flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
        <Button type="submit" variant="primary">
          Rechercher
        </Button>
        <label className="flex items-center gap-2 text-sm text-slate-600">
          <input
            type="checkbox"
            checked={unreadOnly}
            onChange={(e) => setUnreadOnly(e.target.checked)}
            className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
          />
          Non lus
        </label>
      </form>

      {isLoading ? (
        <Spinner />
      ) : displayed.length === 0 ? (
        <p className="py-12 text-center text-slate-400">Aucune notification pour le moment</p>
      ) : (
        <div className="flex flex-col gap-3">
          {displayed.map((n) => {
            const read = isRead(n);
            return (
              <button
                key={n.id}
                type="button"
                onClick={() => handleCardClick(n)}
                className="rounded-lg border border-slate-200 bg-white p-4 text-left shadow-sm transition-shadow hover:shadow-md"
              >
                <div className="flex items-start justify-between gap-3">
                  <h3 className="font-bold text-slate-800">{n.titre}</h3>
                  {!read ? <Badge tone="green">Nouvelle</Badge> : <Badge tone="gray">Lue</Badge>}
                </div>
                <p className="mt-2 whitespace-pre-line text-sm text-slate-600">{n.message}</p>
                <p className="mt-3 text-xs text-slate-400">{formatDateTime(n.dateEnvoi)}</p>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}

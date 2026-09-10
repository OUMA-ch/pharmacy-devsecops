import { apiFetch } from "./client";
import type { Notification } from "../types/api";

export function listNotifications(clientId: number): Promise<Notification[]> {
  return apiFetch<Notification[]>(`/notifications/client/${clientId}`);
}

export function searchNotifications(clientId: number, q: string): Promise<Notification[]> {
  const params = new URLSearchParams({ q });
  return apiFetch<Notification[]>(`/notifications/client/${clientId}/search?${params.toString()}`);
}

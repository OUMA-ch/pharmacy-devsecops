import { apiFetch } from "./client";
import type { LoginInput, RegisterClientInput, UserResponseDTO } from "../types/api";

export function login(input: LoginInput): Promise<UserResponseDTO> {
  return apiFetch<UserResponseDTO>("/auth/login", { method: "POST", body: input });
}

export function registerClient(input: RegisterClientInput): Promise<unknown> {
  return apiFetch("/users/register", {
    method: "POST",
    body: { nomUser: input.nomUser, email: input.email, password: input.password, tele: input.tele }
  });
}

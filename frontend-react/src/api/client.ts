import { clearStoredUser } from "../auth/storage";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

/**
 * Erreur API normalisee. `userMessage` est TOUJOURS sur au affichable tel quel a
 * l'utilisateur (jamais de trace technique brute, cf. README-SECURITY.md).
 * `technicalDetail` est reserve aux logs console en developpement.
 */
export class ApiError extends Error {
  readonly status: number;
  readonly technicalDetail: string;

  constructor(status: number, userMessage: string, technicalDetail: string) {
    super(userMessage);
    this.status = status;
    this.technicalDetail = technicalDetail;
  }
}

interface BackendErrorBody {
  message?: string;
  error?: string;
  status?: string;
  data?: unknown;
}

/**
 * Certains messages backend sont deja des messages metier surs a afficher
 * (ex: ProduitService "Impossible de supprimer ce produit : il a deja ete vendu").
 * D'autres sont des traces SQL/Hibernate brutes (ex: suppression fournisseur
 * reference par une fourniture -> DataIntegrityViolationException) qu'il ne faut
 * jamais montrer telles quelles (cf. section 2 du cahier des charges). On detecte
 * ces cas par heuristique de mots-cles et on les remplace par un message generique.
 */
function toSafeUserMessage(raw: string, status: number): string {
  const lower = raw.toLowerCase();
  const looksTechnical =
    lower.includes("constraint") ||
    lower.includes("violat") ||
    lower.includes("foreign key") ||
    lower.includes("sql") ||
    lower.includes("hibernate") ||
    lower.includes("could not execute statement") ||
    lower.includes("nested exception");

  if (looksTechnical) {
    if (lower.includes("email")) {
      return "Cet email est deja utilise.";
    }
    return "Cette suppression est impossible car cet element est encore utilise ailleurs dans l'application.";
  }

  if (raw.trim().length === 0) {
    return status === 401
      ? "Identifiants incorrects."
      : status === 404
        ? "Ressource introuvable."
        : "Une erreur est survenue. Veuillez reessayer.";
  }

  return raw;
}

interface RequestOptions extends Omit<RequestInit, "body"> {
  body?: unknown;
}

/**
 * Appelle l'API backend et normalise la reponse.
 * Certains controleurs renvoient les donnees brutes, d'autres les enveloppent via
 * ResponseHandler sous la forme { status, message, data } (voir backend/.../ResponseHandler.java).
 * Cette fonction retourne toujours la donnee utile, quel que soit le format.
 */
export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { body: requestBody, headers: optHeaders, ...rest } = options;

  const headers: Record<string, string> = {
    Accept: "application/json",
    ...(optHeaders as Record<string, string> | undefined)
  };

  const init: RequestInit = {
    // Le JWT de session voyage dans un cookie HttpOnly pose par le backend
    // (voir AuthController.login) : "include" est necessaire pour qu'il parte
    // sur les requetes cross-origin (frontend et backend sur des ports/domaines
    // differents). WebConfig.java restreint allowedOrigins a une liste explicite
    // car allowCredentials(true) est incompatible avec allowedOrigins("*").
    credentials: "include",
    ...rest,
    headers
  };
  if (requestBody !== undefined) {
    headers["Content-Type"] = "application/json";
    init.body = JSON.stringify(requestBody);
  }

  let res: Response;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, init);
  } catch (networkError) {
    console.error("[api] network error", networkError);
    throw new ApiError(
      0,
      "Impossible de contacter le serveur. Verifiez votre connexion.",
      String(networkError)
    );
  }

  const text = await res.text();
  let body: unknown = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = text;
    }
  }

  if (!res.ok) {
    const errBody = (body ?? {}) as BackendErrorBody;
    const rawMessage = typeof body === "string" ? body : (errBody.message ?? errBody.error ?? "");
    const userMessage = toSafeUserMessage(rawMessage, res.status);
    console.error(`[api] ${init.method ?? "GET"} ${path} -> ${res.status}`, rawMessage || body);

    // Session expiree/invalide (cookie absent ou JWT perime) : on nettoie l'etat
    // d'affichage local et on renvoie vers /login. On exclut /auth/login lui-meme
    // pour ne pas transformer un simple "mot de passe incorrect" en redirection.
    if (res.status === 401 && !path.startsWith("/auth/login") && typeof window !== "undefined") {
      clearStoredUser();
      if (!window.location.pathname.startsWith("/login")) {
        window.location.href = "/login";
      }
    }

    throw new ApiError(res.status, userMessage, rawMessage || `HTTP ${res.status}`);
  }

  if (
    body &&
    typeof body === "object" &&
    !Array.isArray(body) &&
    "data" in body &&
    "status" in body
  ) {
    return (body as { data: T }).data;
  }
  return body as T;
}

export function apiUrl(path: string): string {
  return `${API_BASE_URL}${path}`;
}

// src/lib/api.ts
const API_BASE_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8081/api";

console.log("🔧 API_BASE_URL configuré:", API_BASE_URL);

import axios, { AxiosError, AxiosInstance, AxiosRequestConfig } from "axios";

class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public response?: Response
  ) {
    super(message);
    this.name = "ApiError";
  }
}

/** Champs utilitaires ajoutés par nous sur la config axios */
type RetriableConfig = AxiosRequestConfig & { _retry?: boolean };

class ApiClient {
  private client: AxiosInstance;

  /** Gestion de rafraîchissement (anti-boucle / anti-spam) */
  private isRefreshing = false;
  private refreshQueue: Array<(token: string) => void> = [];
  private rejectQueue: Array<(err: any) => void> = [];

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000,
      headers: {
        "Content-Type": "application/json",
      },
    });

    // ✅ Si on a déjà un token, on initialise le header par défaut au boot
    const bootToken = this.getStoredAccessToken();
    if (bootToken) {
      this.client.defaults.headers.common[
        "Authorization"
      ] = `Bearer ${bootToken}`;
    }

    this.setupInterceptors();
  }

  /** ============ Interceptors ============ */
  private setupInterceptors() {
    // ➤ Request: injecter l'Authorization si présent
    this.client.interceptors.request.use(
      (config) => {
        const token = this.getStoredAccessToken();
        if (
          token &&
          token !== "null" &&
          token !== "undefined" &&
          config.headers
        ) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        if (import.meta.env.DEV) {
          console.log(
            `🚀 API Request: ${config.method?.toUpperCase()} ${config.baseURL}${
              config.url
            }`
          );
          // Log du header d’auth (utile pour déboguer les 401)
          if (config.headers?.Authorization) {
            console.log(
              "👉 Authorization header:",
              config.headers.Authorization
            );
          } else {
            console.log("👉 Authorization header: (absent)");
          }
          if (config.data) console.log(`📦 Request data:`, config.data);
        }

        return config;
      },
      (error) => {
        console.error("❌ Request interceptor error:", error);
        return Promise.reject(error);
      }
    );

    // ➤ Response: gérer 401 + refresh avec queue
    this.client.interceptors.response.use(
      (response) => {
        if (import.meta.env.DEV) {
          console.log(
            `✅ API Response: ${response.status} ${response.config.url}`
          );
          console.log(`📦 Response data:`, response.data);
        }
        return response;
      },
      async (error: AxiosError) => {
        const originalRequest = (error.config || {}) as RetriableConfig;

        if (import.meta.env.DEV) {
          console.error(
            `❌ API Error: ${error.response?.status ?? "Network"} ${
              originalRequest.url
            }`,
            error.message
          );
          console.error(`📦 Error response:`, error.response?.data);
        }

        // Si 401 → tenter refresh (une seule fois par requête)
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const refreshedToken = await this.getOrRefreshToken();

            // Mettre à jour l'entête pour la requête originale
            originalRequest.headers = originalRequest.headers || {};
            (
              originalRequest.headers as any
            ).Authorization = `Bearer ${refreshedToken}`;

            // Mettre à jour le default pour les futures
            this.client.defaults.headers.common[
              "Authorization"
            ] = `Bearer ${refreshedToken}`;

            // Réessayer la requête originale
            return this.client(originalRequest);
          } catch (refreshError) {
            console.error("❌ Échec du refresh token:", refreshError);
            this.clearAuthAndRedirect();
            return Promise.reject(refreshError);
          }
        }

        // Pour les autres cas, lever une ApiError propre
        throw new ApiError(
          error.response?.status || 500,
          // @ts-ignore
          error.response?.data?.message ||
            error.message ||
            "Une erreur est survenue",
          // @ts-ignore
          error.response
        );
      }
    );
  }

  /** ============ Refresh centralisé avec queue ============ */
  private async getOrRefreshToken(): Promise<string> {
    const current = this.getStoredAccessToken();

    // Petite sécurité : si on a encore un token en local, on tente direct son usage
    if (current && current !== "null" && current !== "undefined") {
      return current;
    }

    const refreshToken = localStorage.getItem("refresh_token");
    if (
      !refreshToken ||
      refreshToken === "null" ||
      refreshToken === "undefined"
    ) {
      throw new Error("Aucun refresh token disponible");
    }

    if (this.isRefreshing) {
      // On s’abonne pour être notifié quand le refresh en cours se termine
      return new Promise<string>((resolve, reject) => {
        this.refreshQueue.push(resolve);
        this.rejectQueue.push(reject);
      });
    }

    this.isRefreshing = true;

    try {
      console.log("🔄 Tentative de refresh du token...");

      // ⚠️ Utiliser axios “brut” pour éviter les intercepteurs (et boucles)
      const refreshResponse = await axios.post(
        `${API_BASE_URL}/auth/refresh`,
        { refreshToken },
        {
          headers: { "Content-Type": "application/json" },
          timeout: 10000,
        }
      );

      // Le backend renvoie ApiResponse<AuthResponse>
      // -> { success, message, data: { access_token, refresh_token, token_type, ... }, timestamp }
      const authData = refreshResponse?.data?.data as {
        access_token: string;
        refresh_token?: string;
        token_type?: string; // ex: "Bearer"
      };

      if (!authData?.access_token) {
        throw new Error("Réponse de refresh invalide (pas d'access_token)");
      }

      const newAccess = authData.access_token;
      const newRefresh = authData.refresh_token;
      const scheme = authData.token_type || "Bearer";

      // Sauvegarder
      localStorage.setItem("auth_token", newAccess);
      if (newRefresh) {
        localStorage.setItem("refresh_token", newRefresh);
      }

      // Mettre à jour defaults pour les prochaines requêtes
      this.client.defaults.headers.common[
        "Authorization"
      ] = `${scheme} ${newAccess}`;

      console.log("✅ Token rafraîchi avec succès");

      // Réveiller la queue
      this.refreshQueue.forEach((res) => res(newAccess));
      this.refreshQueue = [];
      this.rejectQueue = [];

      return newAccess;
    } catch (err) {
      // Réveiller la queue en erreur
      this.rejectQueue.forEach((rej) => rej(err));
      this.refreshQueue = [];
      this.rejectQueue = [];
      throw err;
    } finally {
      this.isRefreshing = false;
    }
  }

  /** ============ Helpers ============ */
  private getStoredAccessToken(): string | null {
    const t = localStorage.getItem("auth_token");
    if (!t || t === "null" || t === "undefined") return null;
    return t;
  }

  private clearAuthAndRedirect() {
    localStorage.removeItem("auth_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("user");

    // Rediriger vers la page de connexion seulement si on n’y est pas déjà
    if (!window.location.pathname.includes("/auth")) {
      const currentPath = window.location.pathname;
      const redirectParam =
        currentPath !== "/"
          ? `?redirect=${encodeURIComponent(currentPath)}`
          : "";
      window.location.href = `/auth${redirectParam}`;
    }
  }

  /** ============ Méthodes HTTP ============ */
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.get<T>(url, config);
    return response.data;
  }

  async post<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.client.post<T>(url, data, config);
    return response.data;
  }

  async put<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.client.put<T>(url, data, config);
    return response.data;
  }

  // ✅ Méthode PATCH
  async patch<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.client.patch<T>(url, data, config);
    return response.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.delete<T>(url, config);
    return response.data;
  }

  async uploadFile<T>(
    url: string,
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<T> {
    const formData = new FormData();
    formData.append("file", file);

    const response = await this.client.post<T>(url, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
      onUploadProgress: (e) => {
        if (onProgress && e.total) {
          const progress = (e.loaded / e.total) * 100;
          onProgress(progress);
        }
      },
    });

    return response.data;
  }

  async testConnection(): Promise<boolean> {
    try {
      await this.get("/health");
      return true;
    } catch {
      return false;
    }
  }
}

export const apiClient = new ApiClient();
export { ApiError };

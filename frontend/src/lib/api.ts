// src/lib/api.ts - VERSION CORRIGÉE AVEC REFRESH TOKEN
const API_BASE_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8081/api";

console.log("🔧 API_BASE_URL configuré:", API_BASE_URL);

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

import axios, { AxiosInstance, AxiosRequestConfig } from "axios";

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000,
      headers: {
        "Content-Type": "application/json",
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor pour ajouter le token
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem("auth_token");
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        if (import.meta.env.DEV) {
          console.log(
            `🚀 API Request: ${config.method?.toUpperCase()} ${config.baseURL}${
              config.url
            }`
          );
          if (config.data) console.log(`📦 Request data:`, config.data);
        }

        return config;
      },
      (error) => {
        console.error("❌ Request interceptor error:", error);
        return Promise.reject(error);
      }
    );

    // Response interceptor pour gérer les erreurs et refresh token
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
      async (error) => {
        if (import.meta.env.DEV) {
          console.error(
            `❌ API Error: ${error.response?.status || "Network"} ${
              error.config?.url
            }`,
            error.message
          );
          console.error(`📦 Error response:`, error.response?.data);
        }

        const originalRequest = error.config;

        // ✅ CORRECTION : Gestion du refresh token avec la bonne structure de données
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const refreshToken = localStorage.getItem("refresh_token");
            if (refreshToken) {
              console.log("🔄 Tentative de refresh du token...");

              const refreshResponse = await this.refreshAuthToken(refreshToken);

              // ✅ CORRECTION : Accès correct aux données dans ApiResponse<AuthResponse>
              const authData = refreshResponse.data.data; // data.data car ApiResponse<AuthResponse>
              const newAccessToken = authData.accessToken; // camelCase comme défini dans AuthResponse
              const newRefreshToken = authData.refreshToken;

              console.log("✅ Token rafraîchi avec succès");

              // Sauvegarder les nouveaux tokens
              localStorage.setItem("auth_token", newAccessToken);
              if (newRefreshToken) {
                localStorage.setItem("refresh_token", newRefreshToken);
              }

              // Mettre à jour l'header pour la requête originale
              originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

              // Mettre à jour l'header par défaut pour les futures requêtes
              this.client.defaults.headers.common[
                "Authorization"
              ] = `Bearer ${newAccessToken}`;

              // Réessayer la requête originale
              return this.client(originalRequest);
            }
          } catch (refreshError) {
            console.error("❌ Échec du refresh token:", refreshError);
            // Nettoyer les tokens et rediriger
            this.clearAuthAndRedirect();
            return Promise.reject(refreshError);
          }
        }

        throw new ApiError(
          error.response?.status || 500,
          error.response?.data?.message ||
            error.message ||
            "Une erreur est survenue",
          error.response
        );
      }
    );
  }

  private async refreshAuthToken(refreshToken: string) {
    // ✅ Faire la requête de refresh sans intercepteur pour éviter la boucle
    return axios.post(
      `${API_BASE_URL}/auth/refresh`,
      { refreshToken },
      {
        headers: {
          "Content-Type": "application/json",
        },
        timeout: 10000,
      }
    );
  }

  private clearAuthAndRedirect() {
    localStorage.removeItem("auth_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("user");

    // Rediriger vers la page de connexion seulement si on n'y est pas déjà
    if (!window.location.pathname.includes("/auth")) {
      const currentPath = window.location.pathname;
      const redirectParam =
        currentPath !== "/"
          ? `?redirect=${encodeURIComponent(currentPath)}`
          : "";
      window.location.href = `/auth${redirectParam}`;
    }
  }

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
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = (progressEvent.loaded / progressEvent.total) * 100;
          onProgress(progress);
        }
      },
    });

    return response.data;
  }

  // Méthode utilitaire pour tester la connexion
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

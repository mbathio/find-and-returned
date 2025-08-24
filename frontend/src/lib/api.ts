// src/lib/api.ts - Configuration API définitivement corrigée

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8081";

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

type RetriableConfig = AxiosRequestConfig & { _retry?: boolean };

class ApiClient {
  private client: AxiosInstance;
  private isRefreshing = false;
  private refreshQueue: Array<(token: string) => void> = [];
  private rejectQueue: Array<(err: unknown) => void> = [];

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000,
      withCredentials: true,
      headers: {
        "Content-Type": "application/json",
      },
    });

    const bootToken = this.getStoredAccessToken();
    if (bootToken) {
      this.client.defaults.headers.common[
        "Authorization"
      ] = `Bearer ${bootToken}`;
    }

    this.setupInterceptors();
  }

  private setupInterceptors() {
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
          if (config.headers?.Authorization) {
            console.log(
              "👉 Authorization header:",
              config.headers.Authorization
            );
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

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const refreshedToken = await this.getOrRefreshToken();
            originalRequest.headers = originalRequest.headers || {};
            (
              originalRequest.headers as Record<string, string>
            ).Authorization = `Bearer ${refreshedToken}`;
            this.client.defaults.headers.common[
              "Authorization"
            ] = `Bearer ${refreshedToken}`;
            return this.client(originalRequest);
          } catch (refreshError) {
            console.error("❌ Échec du refresh token:", refreshError);
            this.clearAuthAndRedirect();
            return Promise.reject(refreshError);
          }
        }

        throw new ApiError(
          error.response?.status || 500,
          // @ts-expect-error: error shape varies
          error.response?.data?.message ||
            error.message ||
            "Une erreur est survenue",
          // @ts-expect-error: response type mismatch
          error.response
        );
      }
    );
  }

  private async getOrRefreshToken(): Promise<string> {
    const current = this.getStoredAccessToken();
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
      return new Promise<string>((resolve, reject) => {
        this.refreshQueue.push(resolve);
        this.rejectQueue.push(reject);
      });
    }

    this.isRefreshing = true;

    try {
      console.log("🔄 Tentative de refresh du token...");

      const refreshResponse = await axios.post(
        `${API_BASE_URL}/api/auth/refresh`,
        { refreshToken },
        {
          headers: { "Content-Type": "application/json" },
          timeout: 10000,
          withCredentials: true,
        }
      );

      const authData = refreshResponse?.data?.data as {
        access_token: string;
        refresh_token?: string;
        token_type?: string;
      };

      if (!authData?.access_token) {
        throw new Error("Réponse de refresh invalide (pas d'access_token)");
      }

      const newAccess = authData.access_token;
      const newRefresh = authData.refresh_token;
      const scheme = authData.token_type || "Bearer";

      localStorage.setItem("auth_token", newAccess);
      if (newRefresh) {
        localStorage.setItem("refresh_token", newRefresh);
      }

      this.client.defaults.headers.common[
        "Authorization"
      ] = `${scheme} ${newAccess}`;

      console.log("✅ Token rafraîchi avec succès");

      this.refreshQueue.forEach((res) => res(newAccess));
      this.refreshQueue = [];
      this.rejectQueue = [];

      return newAccess;
    } catch (err) {
      this.rejectQueue.forEach((rej) => rej(err));
      this.refreshQueue = [];
      this.rejectQueue = [];
      throw err;
    } finally {
      this.isRefreshing = false;
    }
  }

  private getStoredAccessToken(): string | null {
    const t = localStorage.getItem("auth_token");
    if (!t || t === "null" || t === "undefined") return null;
    return t;
  }

  private clearAuthAndRedirect() {
    localStorage.removeItem("auth_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("user");

    if (!window.location.pathname.includes("/auth")) {
      const currentPath = window.location.pathname;
      const redirectParam =
        currentPath !== "/"
          ? `?redirect=${encodeURIComponent(currentPath)}`
          : "";
      window.location.href = `/auth${redirectParam}`;
    }
  }

  // SOLUTION RADICALE: Nettoyer complètement les URLs
  private cleanUrl(url: string): string {
    // Supprimer tous les préfixes /api existants
    let cleanedUrl = url.replace(/^\/api\/+/g, "");
    cleanedUrl = cleanedUrl.replace(/^api\/+/g, "");

    // Assurer qu'on a un seul /api au début
    return `/api/${cleanedUrl}`;
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.get<T>(this.cleanUrl(url), config);
    return response.data;
  }

  async post<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.client.post<T>(
      this.cleanUrl(url),
      data,
      config
    );
    return response.data;
  }

  async put<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.client.put<T>(this.cleanUrl(url), data, config);
    return response.data;
  }

  async patch<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.client.patch<T>(
      this.cleanUrl(url),
      data,
      config
    );
    return response.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.delete<T>(this.cleanUrl(url), config);
    return response.data;
  }

  async uploadFile<T>(
    url: string,
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<T> {
    const formData = new FormData();
    formData.append("file", file);

    const response = await this.client.post<T>(this.cleanUrl(url), formData, {
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
      await this.get("health");
      return true;
    } catch {
      return false;
    }
  }
}

export const apiClient = new ApiClient();
export { ApiError };

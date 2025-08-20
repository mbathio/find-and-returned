// src/lib/api.ts - VERSION CORRIGÉE
const API_BASE_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8081/api"; // ✅ Port corrigé 8081

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

// Configuration Axios avec intercepteurs
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
        
        // Log pour debug en développement
        if (import.meta.env.DEV) {
          console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
        }
        
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor pour gérer les erreurs
    this.client.interceptors.response.use(
      (response) => {
        // Log pour debug en développement
        if (import.meta.env.DEV) {
          console.log(`✅ API Response: ${response.status} ${response.config.url}`);
        }
        return response;
      },
      async (error) => {
        // Log pour debug en développement
        if (import.meta.env.DEV) {
          console.error(`❌ API Error: ${error.response?.status || 'Network'} ${error.config?.url}`, error.message);
        }

        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const refreshToken = localStorage.getItem("refresh_token");
            if (refreshToken) {
              const response = await this.refreshAuthToken(refreshToken);
              localStorage.setItem("auth_token", response.data.accessToken);
              return this.client(originalRequest);
            }
          } catch (refreshError) {
            // Rediriger vers login
            localStorage.removeItem("auth_token");
            localStorage.removeItem("refresh_token");
            window.location.href = "/auth";
          }
        }

        throw new ApiError(
          error.response?.status || 500,
          error.response?.data?.message || error.message || "Une erreur est survenue",
          error.response
        );
      }
    );
  }

  private async refreshAuthToken(refreshToken: string) {
    return this.client.post("/auth/refresh", { refreshToken });
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
      await this.get('/actuator/health');
      return true;
    } catch {
      return false;
    }
  }
}

export const apiClient = new ApiClient();
export { ApiError };
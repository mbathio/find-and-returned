// src/services/auth.ts
import { apiClient } from "@/lib/api";
import { useMutation, useQueryClient } from "@tanstack/react-query";

export interface User {
  id: string;
  name: string;
  email: string;
  phone?: string;
  role: "retrouveur" | "proprietaire" | "mixte";
  email_verified: boolean;
  active: boolean;
  created_at: string;
  last_login_at?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone?: string;
  role?: "retrouveur" | "proprietaire" | "mixte";
}

export interface AuthResponse {
  access_token: string;
  refresh_token: string;
  token_type: string; // "Bearer"
  expires_in: number;
  user: User;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

class AuthService {
  login = async (credentials: LoginRequest): Promise<AuthResponse> => {
    console.log("🚀 AuthService.login - Tentative de connexion");

    const res = await apiClient.post<ApiResponse<AuthResponse>>(
      "/auth/login",
      credentials
    );

    console.log("✅ AuthService.login - Réponse reçue:", res);

    const authData = res.data; // <- ApiResponse<AuthResponse> -> data: AuthResponse
    this.saveAuthData(authData);

    return authData;
  };

  register = async (userData: RegisterRequest): Promise<AuthResponse> => {
    console.log("🚀 AuthService.register - Tentative d'inscription");

    const res = await apiClient.post<ApiResponse<AuthResponse>>(
      "/auth/register",
      userData
    );

    console.log("✅ AuthService.register - Réponse reçue:", res);

    const authData = res.data;
    this.saveAuthData(authData);

    return authData;
    // PS: si l'API n'authentifie pas à l'inscription, adapte en conséquence
  };

  refreshToken = async (refreshToken: string): Promise<AuthResponse> => {
    const res = await apiClient.post<ApiResponse<AuthResponse>>(
      "/auth/refresh",
      { refreshToken }
    );

    const authData = res.data;
    this.saveAuthData(authData);
    return authData;
  };

  logout = async (): Promise<void> => {
    const token = localStorage.getItem("auth_token");
    if (token) {
      try {
        await apiClient.post("/auth/logout", {});
      } catch (error) {
        console.error("Logout error:", error);
      }
    }

    this.clearAuthData();
  };

  getCurrentUser = async (): Promise<User> => {
    console.log(
      "🔍 AuthService.getCurrentUser - Récupération utilisateur actuel"
    );

    const res = await apiClient.get<ApiResponse<User>>("/users/me");

    console.log("✅ AuthService.getCurrentUser - Utilisateur récupéré:", res);

    return res.data;
  };

  /** ====== Local storage helpers ====== */
  private saveAuthData = (authResponse: AuthResponse): void => {
    console.log("💾 Sauvegarde des données d'authentification");

    localStorage.setItem("auth_token", authResponse.access_token);
    localStorage.setItem("refresh_token", authResponse.refresh_token);
    localStorage.setItem("user", JSON.stringify(authResponse.user));

    console.log("✅ Tokens sauvegardés:", {
      access_token: authResponse.access_token ? "✓" : "✗",
      refresh_token: authResponse.refresh_token ? "✓" : "✗",
      user: authResponse.user ? "✓" : "✗",
    });
  };

  private clearAuthData = (): void => {
    console.log("🧹 Nettoyage des données d'authentification");
    localStorage.removeItem("auth_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("user");
  };

  private getStoredUser = (): User | null => {
    const userStr = localStorage.getItem("user");
    return userStr ? JSON.parse(userStr) : null;
  };

  isAuthenticated = (): boolean => {
    const token = localStorage.getItem("auth_token");
    const user = this.getStoredUser();

    const isAuth =
      !!token && token !== "null" && token !== "undefined" && !!user;

    if (import.meta.env.DEV) {
      console.log("🔐 Vérification authentification:", {
        hasToken: !!token,
        hasUser: !!user,
        isAuthenticated: isAuth,
      });
    }

    return isAuth;
  };

  getToken = (): string | null => {
    const t = localStorage.getItem("auth_token");
    if (!t || t === "null" || t === "undefined") return null;
    return t;
  };
}

export const authService = new AuthService();

/** ========= React Query hooks ========= */
export const useLogin = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authService.login,
    onSuccess: (data) => {
      console.log("✅ useLogin.onSuccess - Connexion réussie, data:", data);
      queryClient.setQueryData(["currentUser"], data.user);
      queryClient.invalidateQueries({ queryKey: ["currentUser"] });
    },
    onError: (error) => {
      console.error("❌ useLogin.onError:", error);
    },
  });
};

export const useRegister = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authService.register,
    onSuccess: (data) => {
      console.log(
        "✅ useRegister.onSuccess - Inscription réussie, data:",
        data
      );
      queryClient.setQueryData(["currentUser"], data.user);
      queryClient.invalidateQueries({ queryKey: ["currentUser"] });
    },
    onError: (error) => {
      console.error("❌ useRegister.onError:", error);
    },
  });
};

export const useLogout = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authService.logout,
    onSuccess: () => {
      console.log("✅ useLogout.onSuccess - Déconnexion réussie");
      queryClient.clear();
      window.location.href = "/";
    },
    onError: (error) => {
      console.error("❌ useLogout.onError:", error);
      // Même en cas d'erreur, nettoyer localement
      authService["clearAuthData"]?.();
      queryClient.clear();
      window.location.href = "/";
    },
  });
};

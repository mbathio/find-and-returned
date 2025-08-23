// src/services/auth.ts - CORRECTION DES HOOKS POUR SYNCHRONISER AVEC LE CONTEXTE
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
  token_type: string;
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

    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      "/auth/login",
      credentials
    );

    console.log("✅ AuthService.login - Réponse reçue:", response);

    // ✅ CORRECTION : Accès correct aux données dans ApiResponse<AuthResponse>
    const authData = response.data;

    // Sauvegarder les tokens immédiatement
    this.saveAuthData(authData);

    return authData;
  };

  register = async (userData: RegisterRequest): Promise<AuthResponse> => {
    console.log("🚀 AuthService.register - Tentative d'inscription");

    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      "/auth/register",
      userData
    );

    console.log("✅ AuthService.register - Réponse reçue:", response);

    const authData = response.data;

    // Sauvegarder les tokens immédiatement
    this.saveAuthData(authData);

    return authData;
  };

  refreshToken = async (refreshToken: string): Promise<AuthResponse> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      "/auth/refresh",
      { refreshToken }
    );

    const authData = response.data;
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

    const response = await apiClient.get<ApiResponse<User>>("/users/me");

    console.log(
      "✅ AuthService.getCurrentUser - Utilisateur récupéré:",
      response
    );

    return response.data;
  };

  saveAuthData = (authResponse: AuthResponse): void => {
    console.log("💾 Sauvegarde des données d'authentification");

    // Sauvegarder les tokens avec les noms corrects
    localStorage.setItem("auth_token", authResponse.access_token);
    localStorage.setItem("refresh_token", authResponse.refresh_token);
    localStorage.setItem("user", JSON.stringify(authResponse.user));

    console.log("✅ Tokens sauvegardés:", {
      access_token: authResponse.access_token ? "✓" : "✗",
      refresh_token: authResponse.refresh_token ? "✓" : "✗",
      user: authResponse.user ? "✓" : "✗",
    });
  };

  clearAuthData = (): void => {
    console.log("🧹 Nettoyage des données d'authentification");

    localStorage.removeItem("auth_token");
    localStorage.removeItem("refresh_token");
    localStorage.removeItem("user");
  };

  getStoredUser = (): User | null => {
    const userStr = localStorage.getItem("user");
    return userStr ? JSON.parse(userStr) : null;
  };

  isAuthenticated = (): boolean => {
    const token = localStorage.getItem("auth_token");
    const user = this.getStoredUser();

    const isAuth = !!(token && user);

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
    return localStorage.getItem("auth_token");
  };
}

export const authService = new AuthService();

// ✅ CORRECTION : Hooks qui utilisent le contexte AuthContext
// Ces hooks doivent être utilisés AVEC useAuth() dans les composants
export const useLogin = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: authService.login,
    onSuccess: (data) => {
      console.log("✅ useLogin.onSuccess - Connexion réussie, data:", data);

      // ✅ IMPORTANT : Ne pas essayer d'accéder au contexte ici
      // Le composant qui utilise ce hook doit appeler authContext.login()

      // Mettre à jour React Query
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

      // ✅ IMPORTANT : Ne pas essayer d'accéder au contexte ici
      // Le composant qui utilise ce hook doit appeler authContext.login()

      // Mettre à jour React Query
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
      authService.clearAuthData();
      queryClient.clear();
      window.location.href = "/";
    },
  });
};

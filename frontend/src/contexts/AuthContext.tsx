// src/contexts/AuthContext.tsx - VERSION CORRIGÉE
import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { User, authService } from "@/services/auth";
import { useQuery, useQueryClient } from "@tanstack/react-query";

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (user: User) => void;
  logout: () => void;
  updateUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);
  const queryClient = useQueryClient();

  // ✅ CORRECTION : Initialisation de l'état d'authentification au démarrage
  useEffect(() => {
    const initAuth = () => {
      const storedUser = authService.getStoredUser();
      const hasToken = !!localStorage.getItem("auth_token");

      if (hasToken && storedUser) {
        setUser(storedUser);
        setIsAuthenticated(true);
      }
      setIsInitialized(true);
    };

    initAuth();
  }, []);

  // ✅ CORRECTION : Query pour charger l'utilisateur actuel seulement si authentifié
  const { data: currentUser, isLoading: isLoadingUser } = useQuery({
    queryKey: ["currentUser"],
    queryFn: authService.getCurrentUser,
    enabled: isAuthenticated && isInitialized, // ✅ Seulement si authentifié
    retry: (failureCount, error: any) => {
      // Ne pas retry sur 401 (non autorisé)
      if (error?.status === 401) {
        // Token invalide, déconnecter automatiquement
        logout();
        return false;
      }
      return failureCount < 1;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Mettre à jour l'état quand l'utilisateur actuel change
  useEffect(() => {
    if (currentUser) {
      setUser(currentUser);
      setIsAuthenticated(true);
      // Mettre à jour le localStorage
      localStorage.setItem("user", JSON.stringify(currentUser));
    }
  }, [currentUser]);

  const login = (userData: User) => {
    setUser(userData);
    setIsAuthenticated(true);
    queryClient.setQueryData(["currentUser"], userData);
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      setUser(null);
      setIsAuthenticated(false);
      queryClient.clear();
    }
  };

  const updateUser = (userData: User) => {
    setUser(userData);
    localStorage.setItem("user", JSON.stringify(userData));
    queryClient.setQueryData(["currentUser"], userData);
  };

  // ✅ CORRECTION : isLoading ne doit être true que pendant le chargement initial
  const isLoading = !isInitialized || (isAuthenticated && isLoadingUser);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    login,
    logout,
    updateUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
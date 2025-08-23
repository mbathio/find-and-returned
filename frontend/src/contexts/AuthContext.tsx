// src/contexts/AuthContext.tsx - CORRECTION FINALE AVEC TYPES PROPRES
import {
  createContext,
  useContext,
  useState,
  useEffect,
  ReactNode,
  useTransition,
  startTransition,
  useCallback,
} from "react";
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

// ✅ CORRECTION : Export séparé du hook pour éviter l'erreur fast-refresh
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

// ✅ CORRECTION : Définir les types d'erreur properly
interface AuthError {
  status?: number;
  message?: string;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);
  const [isPending, startTransition] = useTransition();
  const queryClient = useQueryClient();

  // ✅ CORRECTION : Fonction logout stable avec useCallback
  const logout = useCallback(async () => {
    console.log("🚪 AuthContext.logout - Déconnexion");

    try {
      await authService.logout();
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      startTransition(() => {
        setUser(null);
        setIsAuthenticated(false);
        queryClient.clear();
      });
    }
  }, [queryClient]);

  // ✅ CORRECTION 1 : Initialisation immédiate au démarrage
  useEffect(() => {
    const initAuth = () => {
      console.log("🔧 AuthContext - Initialisation de l'authentification");

      startTransition(() => {
        const storedUser = authService.getStoredUser();
        const hasToken = !!localStorage.getItem("auth_token");

        console.log("🔍 Données stockées:", {
          hasToken,
          hasUser: !!storedUser,
          user: storedUser,
        });

        if (hasToken && storedUser) {
          console.log("✅ Utilisateur trouvé, authentification activée");
          setUser(storedUser);
          setIsAuthenticated(true);
        } else {
          console.log("❌ Pas de données d'authentification valides");
          setUser(null);
          setIsAuthenticated(false);
        }
        setIsInitialized(true);
      });
    };

    initAuth();
  }, []);

  // ✅ CORRECTION 2 : Query conditionnelle avec types propres
  const {
    data: currentUser,
    isLoading: isLoadingUser,
    error,
  } = useQuery({
    queryKey: ["currentUser"],
    queryFn: authService.getCurrentUser,
    enabled: isAuthenticated && isInitialized,
    retry: (failureCount, error: unknown) => {
      console.log("🔄 Retry currentUser query:", { failureCount, error });

      // ✅ CORRECTION : Type guard proper pour l'erreur
      const authError = error as AuthError;
      if (authError?.status === 401) {
        console.log("❌ Token invalide, déconnexion automatique");
        startTransition(() => {
          logout();
        });
        return false;
      }
      return failureCount < 1;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // ✅ CORRECTION 3 : Gestion des erreurs avec types propres
  useEffect(() => {
    if (error) {
      console.error(
        "❌ Erreur lors de la récupération de l'utilisateur:",
        error
      );
      const authError = error as AuthError;
      if (authError?.status === 401) {
        startTransition(() => {
          logout();
        });
      }
    }
  }, [error, logout]); // ✅ Ajout de logout dans les deps

  // ✅ CORRECTION 4 : Mettre à jour quand currentUser change
  useEffect(() => {
    if (currentUser && isAuthenticated) {
      console.log("🔄 Mise à jour des données utilisateur:", currentUser);
      startTransition(() => {
        setUser(currentUser);
        localStorage.setItem("user", JSON.stringify(currentUser));
      });
    }
  }, [currentUser, isAuthenticated]);

  // ✅ CORRECTION 5 : Fonction login qui met à jour immédiatement l'état
  const login = useCallback(
    (userData: User) => {
      console.log(
        "✅ AuthContext.login - Connexion de l'utilisateur:",
        userData
      );

      startTransition(() => {
        setUser(userData);
        setIsAuthenticated(true);

        // Mettre à jour React Query immédiatement
        queryClient.setQueryData(["currentUser"], userData);

        // S'assurer que le localStorage est à jour
        localStorage.setItem("user", JSON.stringify(userData));
      });
    },
    [queryClient]
  );

  // ✅ CORRECTION 6 : Fonction updateUser
  const updateUser = useCallback(
    (userData: User) => {
      console.log("🔄 AuthContext.updateUser:", userData);

      startTransition(() => {
        setUser(userData);
        localStorage.setItem("user", JSON.stringify(userData));
        queryClient.setQueryData(["currentUser"], userData);
      });
    },
    [queryClient]
  );

  // ✅ CORRECTION 7 : isLoading correct
  const isLoading = !isInitialized || isPending || isLoadingUser;

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    login,
    logout,
    updateUser,
  };

  // ✅ CORRECTION 8 : Debug en développement
  if (import.meta.env.DEV) {
    console.log("🔧 AuthContext State:", {
      user: user?.name || null,
      isAuthenticated,
      isLoading,
      isInitialized,
      hasToken: !!localStorage.getItem("auth_token"),
    });
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

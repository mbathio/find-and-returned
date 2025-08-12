// src/components/providers/NotificationProvider.tsx
import { createContext, useContext, useEffect, ReactNode } from "react";
import { toast } from "@/hooks/use-toast";

interface NotificationContextType {
  requestPermission: () => Promise<boolean>;
  showNotification: (title: string, options?: NotificationOptions) => void;
}

// Export the context so it can be imported in use-notifications.ts
export const NotificationContext = createContext<
  NotificationContextType | undefined
>(undefined);

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error(
      "useNotifications must be used within a NotificationProvider"
    );
  }
  return context;
};

interface NotificationProviderProps {
  children: ReactNode;
}

export const NotificationProvider = ({
  children,
}: NotificationProviderProps) => {
  const requestPermission = async (): Promise<boolean> => {
    if (!("Notification" in window)) {
      console.warn("Ce navigateur ne supporte pas les notifications");
      return false;
    }

    if (Notification.permission === "granted") {
      return true;
    }

    if (Notification.permission === "denied") {
      return false;
    }

    const permission = await Notification.requestPermission();
    return permission === "granted";
  };

  const showNotification = (title: string, options?: NotificationOptions) => {
    if (Notification.permission === "granted") {
      new Notification(title, {
        icon: "/icon-192x192.png",
        badge: "/badge-72x72.png",
        ...options,
      });
    } else {
      // Fallback vers toast si pas de permission
      toast({
        title,
        description: options?.body,
      });
    }
  };

  // Écouter les messages du service worker
  useEffect(() => {
    if ("serviceWorker" in navigator) {
      navigator.serviceWorker.addEventListener("message", (event) => {
        if (event.data && event.data.type === "NOTIFICATION") {
          showNotification(event.data.title, event.data.options);
        }
      });
    }
  }, []);

  return (
    <NotificationContext.Provider
      value={{ requestPermission, showNotification }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

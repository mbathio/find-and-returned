// src/contexts/NotificationContext.tsx
import { createContext } from "react";

export interface NotificationContextType {
  requestPermission: () => Promise<boolean>;
  showNotification: (title: string, options?: NotificationOptions) => void;
}

export const NotificationContext = createContext<
  NotificationContextType | undefined
>(undefined);

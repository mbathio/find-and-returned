// src/utils/dateFormatter.ts - VERSION CORRIGÉE
export const formatForLocalDateTime = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  const seconds = String(date.getSeconds()).padStart(2, "0");

  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
};

// Alias pour compatibilité
export const formatLocalDateTime = formatForLocalDateTime;

// Fonction utilitaire pour formater une date en string ISO simple
export const formatDateForInput = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};

// Fonction pour parser une date depuis un input HTML
export const parseDateFromInput = (dateString: string): Date => {
  return new Date(dateString + "T00:00:00");
};

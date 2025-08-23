// src/services/messages.ts - VERSION CORRIGÉE POUR REACT QUERY V5
import { apiClient } from "@/lib/api";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { authService } from "@/services/auth";

export interface User {
  id: string;
  name: string;
  email?: string;
  phone?: string;
  role: string;
}

export interface Listing {
  id: string;
  title: string;
  category: string;
  locationText: string;
  status: string;
}

export interface Message {
  id: string;
  thread_id: string;
  sender_user: User;
  body: string;
  message_type: "text" | "system";
  is_read: boolean;
  read_at?: string;
  created_at: string;
}

export interface Thread {
  id: string;
  listing: Listing;
  owner_user: User;
  finder_user: User;
  status: "active" | "closed";
  approved_by_owner?: boolean;
  approved_by_finder?: boolean;
  last_message_at?: string;
  unread_count?: number;
  last_message?: Message;
  created_at: string;
  updated_at: string;
}

export interface CreateMessageRequest {
  threadId: string;
  body: string;
  messageType?: "text" | "system";
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PagedResponse<T> {
  items: T[];
  total: number;
  page: number;
  totalPages: number;
}

class MessagesService {
  async getThreads(
    status?: string,
    page = 1,
    pageSize = 20
  ): Promise<PagedResponse<Thread>> {
    const params = new URLSearchParams({
      page: page.toString(),
      pageSize: pageSize.toString(),
    });

    if (status) {
      params.append("status", status);
    }

    const response = await apiClient.get<ApiResponse<PagedResponse<Thread>>>(
      `/threads?${params.toString()}`
    );
    return response.data;
  }

  async getThread(id: string): Promise<Thread> {
    const response = await apiClient.get<ApiResponse<Thread>>(`/threads/${id}`);
    return response.data;
  }

  async createThread(listingId: string): Promise<Thread> {
    const response = await apiClient.post<ApiResponse<Thread>>(
      `/threads?listingId=${listingId}`
    );
    return response.data;
  }

  async closeThread(id: string): Promise<Thread> {
    const response = await apiClient.patch<ApiResponse<Thread>>(
      `/threads/${id}/close`
    );
    return response.data;
  }

  async getMessages(
    threadId: string,
    page = 1,
    pageSize = 50
  ): Promise<PagedResponse<Message>> {
    const params = new URLSearchParams({
      page: page.toString(),
      pageSize: pageSize.toString(),
    });

    const response = await apiClient.get<ApiResponse<PagedResponse<Message>>>(
      `/messages/thread/${threadId}?${params.toString()}`
    );
    return response.data;
  }

  async sendMessage(messageData: CreateMessageRequest): Promise<Message> {
    const response = await apiClient.post<ApiResponse<Message>>(
      "/messages",
      messageData
    );
    return response.data;
  }

  async markThreadAsRead(threadId: string): Promise<void> {
    await apiClient.patch<ApiResponse<void>>(
      `/messages/thread/${threadId}/read`
    );
  }

  async getUnreadCount(): Promise<number> {
    const response = await apiClient.get<ApiResponse<number>>(
      "/messages/unread-count"
    );
    return response.data;
  }
}

export const messagesService = new MessagesService();

// Hooks React Query avec gestion d'authentification
export const useThreads = (status?: string, page = 1, pageSize = 20) => {
  return useQuery({
    queryKey: ["threads", status, page, pageSize],
    queryFn: () => messagesService.getThreads(status, page, pageSize),
    enabled: authService.isAuthenticated(),
    staleTime: 30 * 1000, // 30 secondes
  });
};

export const useThread = (id: string) => {
  return useQuery({
    queryKey: ["thread", id],
    queryFn: () => messagesService.getThread(id),
    enabled: !!id && authService.isAuthenticated(),
    staleTime: 10 * 1000, // 10 secondes
  });
};

export const useMessages = (threadId: string, page = 1, pageSize = 50) => {
  return useQuery({
    queryKey: ["messages", threadId, page, pageSize],
    queryFn: () => messagesService.getMessages(threadId, page, pageSize),
    enabled: !!threadId && authService.isAuthenticated(),
    staleTime: 5 * 1000, // 5 secondes
  });
};

export const useCreateThread = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: messagesService.createThread,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["threads"] });
    },
  });
};

export const useSendMessage = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: messagesService.sendMessage,
    onSuccess: (data) => {
      // Invalider les messages du thread
      queryClient.invalidateQueries({
        queryKey: ["messages", data.thread_id],
      });
      // Invalider la liste des threads
      queryClient.invalidateQueries({ queryKey: ["threads"] });
    },
  });
};

export const useMarkAsRead = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: messagesService.markThreadAsRead,
    onSuccess: (_, threadId) => {
      queryClient.invalidateQueries({
        queryKey: ["messages", threadId],
      });
      queryClient.invalidateQueries({ queryKey: ["threads"] });
      queryClient.invalidateQueries({ queryKey: ["unreadCount"] });
    },
  });
};

// ✅ CORRECTION : Hook compatible React Query v5
export const useUnreadCount = () => {
  const isAuth = authService.isAuthenticated();

  return useQuery({
    queryKey: ["unreadCount"],
    queryFn: messagesService.getUnreadCount,
    enabled: isAuth, // ✅ Condition d'activation stricte
    staleTime: 10 * 1000, // 10 secondes
    refetchInterval: isAuth ? 30 * 1000 : false, // ✅ Pas de refetch automatique si non authentifié
    retry: false, // ✅ Pas de retry sur erreur pour éviter les boucles
    // ✅ SUPPRIMÉ: suspense (n'existe plus dans React Query v5)
  });
};

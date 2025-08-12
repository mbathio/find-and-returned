// src/services/listings.ts
import { apiClient } from "@/lib/api";
import {
  useQuery,
  useMutation,
  useQueryClient,
  UseQueryOptions,
} from "@tanstack/react-query";

export interface Listing {
  id: string;
  title: string;
  category: string;
  locationText: string;
  latitude?: number;
  longitude?: number;
  foundAt: string;
  description: string;
  imageUrl?: string;
  status: "active" | "resolved";
  finderUserId: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateListingRequest {
  title: string;
  category: string;
  locationText: string;
  latitude?: number;
  longitude?: number;
  foundAt: string;
  description: string;
  imageUrl?: string;
}

export interface ListingsSearchParams {
  q?: string;
  category?: string;
  location?: string;
  lat?: number;
  lng?: number;
  radius_km?: number;
  date_from?: string;
  date_to?: string;
  page?: number;
  page_size?: number;
}

export interface ListingsResponse {
  items: Listing[];
  total: number;
  page: number;
  totalPages: number;
}

class ListingsService {
  private readonly baseUrl = "/listings";

  async getListings(
    params: ListingsSearchParams = {}
  ): Promise<ListingsResponse> {
    const searchParams = new URLSearchParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        searchParams.append(key, value.toString());
      }
    });

    const url = `${this.baseUrl}${
      searchParams.toString() ? `?${searchParams.toString()}` : ""
    }`;
    return apiClient.get<ListingsResponse>(url);
  }

  async getListing(id: string): Promise<Listing> {
    return apiClient.get<Listing>(`${this.baseUrl}/${id}`);
  }

  async createListing(data: CreateListingRequest): Promise<Listing> {
    return apiClient.post<Listing>(this.baseUrl, data);
  }

  async updateListing(
    id: string,
    data: Partial<CreateListingRequest>
  ): Promise<Listing> {
    return apiClient.put<Listing>(`${this.baseUrl}/${id}`, data);
  }

  async deleteListing(id: string): Promise<void> {
    return apiClient.delete<void>(`${this.baseUrl}/${id}`);
  }

  async uploadImage(
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<{ url: string }> {
    return apiClient.uploadFile<{ url: string }>(
      "/upload/image",
      file,
      onProgress
    );
  }
}

export const listingsService = new ListingsService();

// Hooks React Query pour les listings
export const useListings = (
  params: ListingsSearchParams = {},
  options?: Omit<UseQueryOptions<ListingsResponse>, "queryKey" | "queryFn">
) => {
  return useQuery({
    queryKey: ["listings", params],
    queryFn: () => listingsService.getListings(params),
    staleTime: 2 * 60 * 1000, // 2 minutes
    ...options,
  });
};

export const useListing = (id: string) => {
  return useQuery({
    queryKey: ["listing", id],
    queryFn: () => listingsService.getListing(id),
    enabled: !!id,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

export const useCreateListing = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: listingsService.createListing,
    onSuccess: () => {
      // Invalider le cache des listings
      queryClient.invalidateQueries({ queryKey: ["listings"] });
    },
  });
};

export const useUpdateListing = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: string;
      data: Partial<CreateListingRequest>;
    }) => listingsService.updateListing(id, data),
    onSuccess: (data) => {
      // Mettre à jour le cache spécifique
      queryClient.setQueryData(["listing", data.id], data);
      // Invalider la liste
      queryClient.invalidateQueries({ queryKey: ["listings"] });
    },
  });
};

export const useDeleteListing = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: listingsService.deleteListing,
    onSuccess: (_, id) => {
      // Supprimer du cache
      queryClient.removeQueries({ queryKey: ["listing", id] });
      // Invalider la liste
      queryClient.invalidateQueries({ queryKey: ["listings"] });
    },
  });
};

export const useUploadImage = () => {
  return useMutation({
    mutationFn: ({
      file,
      onProgress,
    }: {
      file: File;
      onProgress?: (progress: number) => void;
    }) => listingsService.uploadImage(file, onProgress),
  });
};

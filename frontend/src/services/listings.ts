// src/services/listings.ts - VERSION CORRIGÉE - URLs sans double préfixe
import { apiClient } from "@/lib/api";
import {
  useQuery,
  useMutation,
  useQueryClient,
  UseQueryOptions,
} from "@tanstack/react-query";

// Interface conforme au backend ListingResponse
export interface Listing {
  id: string;
  title: string;
  category:
    | "cles"
    | "electronique"
    | "bagagerie"
    | "documents"
    | "vetements"
    | "autre";
  locationText: string;
  latitude?: number;
  longitude?: number;
  foundAt: string; // ISO string
  description: string;
  imageUrl?: string;
  status: "active" | "resolved";
  finderUserId: string;
  createdAt: string;
  updatedAt: string;
}

// Interface conforme au backend CreateListingRequest
export interface CreateListingRequest {
  title: string;
  category: string;
  locationText: string;
  latitude?: number;
  longitude?: number;
  foundAt: string; // LocalDateTime ISO string
  description: string;
  imageUrl?: string;
}

// Paramètres de recherche conformes à l'API backend
export interface ListingsSearchParams {
  q?: string;
  category?: string;
  location?: string;
  lat?: number;
  lng?: number;
  radius_km?: number;
  date_from?: string; // YYYY-MM-DD
  date_to?: string; // YYYY-MM-DD
  page?: number;
  page_size?: number;
}

// Interface conforme au backend PagedResponse
export interface ListingsResponse {
  items: Listing[];
  total: number;
  page: number;
  totalPages: number;
}

// Interface conforme au backend ApiResponse
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

class ListingsService {
  // ✅ CORRECTION IMPORTANTE: URLs relatives SANS préfixe
  // apiClient.baseURL = "http://localhost:8081/api"
  // Donc "listings" → "http://localhost:8081/api/listings" ✅
  private readonly baseUrl = "listings";

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

    if (import.meta.env.DEV) {
      console.log("🔍 Fetching listings with URL:", url);
      console.log("🔍 Search params:", params);
    }

    const response = await apiClient.get<ApiResponse<ListingsResponse>>(url);
    return response.data;
  }

  async getListing(id: string): Promise<Listing> {
    const response = await apiClient.get<ApiResponse<Listing>>(
      `${this.baseUrl}/${id}`
    );
    return response.data;
  }

  async createListing(data: CreateListingRequest): Promise<Listing> {
    console.log("🚀 Creating listing with data:", data);
    console.log("🔗 URL finale:", `${this.baseUrl}`); // listings → /api/listings ✅

    const response = await apiClient.post<ApiResponse<Listing>>(
      this.baseUrl, // ✅ "listings" → URL finale: http://localhost:8081/api/listings
      data
    );
    return response.data;
  }

  async updateListing(
    id: string,
    data: Partial<CreateListingRequest>
  ): Promise<Listing> {
    const response = await apiClient.put<ApiResponse<Listing>>(
      `${this.baseUrl}/${id}`,
      data
    );
    return response.data;
  }

  async deleteListing(id: string): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }

  async uploadImage(
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<{ url: string }> {
    // ✅ CORRECTION CRITIQUE: URL directe SANS préfixe
    // "upload/image" → "http://localhost:8081/api/upload/image" ✅
    console.log("📤 Uploading image, URL finale: upload/image");

    const response = await apiClient.uploadFile<ApiResponse<{ url: string }>>(
      "upload/image", // ✅ URL directe → http://localhost:8081/api/upload/image
      file,
      onProgress
    );
    return response.data;
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
      queryClient.invalidateQueries({ queryKey: ["listings"] });
    },
    onError: (error) => {
      console.error("❌ Create listing error:", error);
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
      queryClient.setQueryData(["listing", data.id], data);
      queryClient.invalidateQueries({ queryKey: ["listings"] });
    },
  });
};

export const useDeleteListing = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: listingsService.deleteListing,
    onSuccess: (_, id) => {
      queryClient.removeQueries({ queryKey: ["listing", id] });
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

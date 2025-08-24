// src/services/listings.ts - VERSION AVEC LOGS DE DEBUG DÉTAILLÉS
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

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

const LISTINGS_ENDPOINT = "listings";
const UPLOAD_ENDPOINT = "upload/image";

class ListingsService {
  async getListings(
    params: ListingsSearchParams = {}
  ): Promise<ListingsResponse> {
    console.log("🔍 ListingsService.getListings - DÉBUT");
    console.log("🔍 Paramètres reçus:", params);

    const searchParams = new URLSearchParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        searchParams.append(key, value.toString());
      }
    });

    const url = searchParams.toString()
      ? `${LISTINGS_ENDPOINT}?${searchParams.toString()}`
      : LISTINGS_ENDPOINT;

    console.log("🔍 URL construite:", url);

    try {
      console.log("🔍 Appel apiClient.get...");
      const response = await apiClient.get<ApiResponse<ListingsResponse>>(url);

      console.log("🔍 Response RAW (ce que retourne apiClient.get):");
      console.log("🔍 - Type:", typeof response);
      console.log("🔍 - Valeur:", response);
      console.log("🔍 - JSON stringifié:", JSON.stringify(response, null, 2));

      // Vérifier si response a une propriété 'data'
      if (response && typeof response === "object") {
        console.log("🔍 Properties de response:", Object.keys(response));

        if ("data" in response) {
          console.log("🔍 response.data existe:");
          console.log("🔍 - Type de response.data:", typeof response.data);
          console.log("🔍 - Valeur de response.data:", response.data);
          console.log(
            "🔍 - JSON stringifié de response.data:",
            JSON.stringify(response.data, null, 2)
          );

          if (
            response.data &&
            typeof response.data === "object" &&
            "items" in response.data
          ) {
            console.log("🔍 response.data.items existe:", response.data.items);
            console.log(
              "🔍 Nombre d'items:",
              Array.isArray(response.data.items)
                ? response.data.items.length
                : "pas un array"
            );
          } else {
            console.log("❌ response.data n'a pas de propriété 'items'");
          }

          return response.data as ListingsResponse;
        } else {
          console.log("❌ response n'a pas de propriété 'data'");
          // Peut-être que response EST déjà la ListingsResponse ?
          if ("items" in response) {
            console.log("✅ response a directement une propriété 'items'");
            return response as ListingsResponse;
          }
        }
      }

      console.log("❌ Structure de response inattendue");
      throw new Error("Structure de réponse inattendue");
    } catch (error) {
      console.error("❌ Erreur dans getListings:");
      console.error("❌ Type d'erreur:", typeof error);
      console.error("❌ Erreur:", error);
      if (error instanceof Error) {
        console.error("❌ Message:", error.message);
        console.error("❌ Stack:", error.stack);
      }
      throw error;
    }
  }

  async getListing(id: string): Promise<Listing> {
    const url = `${LISTINGS_ENDPOINT}/${id}`;
    const response = await apiClient.get<ApiResponse<Listing>>(url);
    return response.data;
  }

  async createListing(data: CreateListingRequest): Promise<Listing> {
    const response = await apiClient.post<ApiResponse<Listing>>(
      LISTINGS_ENDPOINT,
      data
    );
    return response.data;
  }

  async updateListing(
    id: string,
    data: Partial<CreateListingRequest>
  ): Promise<Listing> {
    const url = `${LISTINGS_ENDPOINT}/${id}`;
    const response = await apiClient.put<ApiResponse<Listing>>(url, data);
    return response.data;
  }

  async deleteListing(id: string): Promise<void> {
    const url = `${LISTINGS_ENDPOINT}/${id}`;
    await apiClient.delete<ApiResponse<void>>(url);
  }

  async uploadImage(
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<{ url: string }> {
    const response = await apiClient.uploadFile<ApiResponse<{ url: string }>>(
      UPLOAD_ENDPOINT,
      file,
      onProgress
    );
    return response.data;
  }
}

export const listingsService = new ListingsService();

export const useListings = (
  params: ListingsSearchParams = {},
  options?: Omit<UseQueryOptions<ListingsResponse>, "queryKey" | "queryFn">
) => {
  return useQuery({
    queryKey: ["listings", params],
    queryFn: async () => {
      console.log("🔍 useListings queryFn - DÉBUT");
      try {
        const result = await listingsService.getListings(params);
        console.log("🔍 useListings queryFn - RÉSULTAT:");
        console.log("🔍 - Type:", typeof result);
        console.log("🔍 - Valeur:", result);
        console.log("🔍 - JSON stringifié:", JSON.stringify(result, null, 2));
        return result;
      } catch (error) {
        console.error("❌ useListings queryFn - ERREUR:", error);
        throw error;
      }
    },
    staleTime: 2 * 60 * 1000,
    ...options,
  });
};

export const useListing = (id: string) => {
  return useQuery({
    queryKey: ["listing", id],
    queryFn: () => listingsService.getListing(id),
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
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

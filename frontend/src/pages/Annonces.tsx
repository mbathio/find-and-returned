// src/pages/Annonces.tsx - VERSION CORRIGÉE
import { Helmet } from "react-helmet-async";
import { Input } from "@/components/ui/input";
import { Select, SelectTrigger, SelectContent, SelectItem, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { useState, useEffect } from "react";
import { cn } from "@/lib/utils";
import { format } from "date-fns";
import { fr } from "date-fns/locale";
import { Search, Calendar as CalendarIcon, MapPin, Loader2 } from "lucide-react";
import ListingCard from "@/components/listings/ListingCard";
import { useListings, ListingsSearchParams } from "@/services/listings";
import { useSearchParams } from "react-router-dom";
import { Card, CardContent } from "@/components/ui/card";

const Annonces = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  
  // États des filtres
  const [filters, setFilters] = useState<ListingsSearchParams>({
    q: searchParams.get("q") || "",
    location: searchParams.get("location") || "",
    category: searchParams.get("category") || "",
    page: parseInt(searchParams.get("page") || "1"),
    page_size: 20,
  });

  const [dateFrom, setDateFrom] = useState<Date | undefined>(
    searchParams.get("date_from") ? new Date(searchParams.get("date_from")!) : undefined
  );
  const [dateTo, setDateTo] = useState<Date | undefined>(
    searchParams.get("date_to") ? new Date(searchParams.get("date_to")!) : undefined
  );

  // Query pour charger les annonces
  const { data: listingsData, isLoading, error } = useListings({
    ...filters,
    date_from: dateFrom ? format(dateFrom, "yyyy-MM-dd") : undefined,
    date_to: dateTo ? format(dateTo, "yyyy-MM-dd") : undefined,
  });

  // Mettre à jour l'URL quand les filtres changent
  useEffect(() => {
    const params = new URLSearchParams();
    
    if (filters.q) params.set("q", filters.q);
    if (filters.location) params.set("location", filters.location);
    if (filters.category) params.set("category", filters.category);
    if (filters.page && filters.page > 1) params.set("page", filters.page.toString());
    if (dateFrom) params.set("date_from", format(dateFrom, "yyyy-MM-dd"));
    if (dateTo) params.set("date_to", format(dateTo, "yyyy-MM-dd"));

    setSearchParams(params);
  }, [filters, dateFrom, dateTo, setSearchParams]);

  const handleSearch = () => {
    setFilters(prev => ({ ...prev, page: 1 }));
  };

  const handleResetFilters = () => {
    setFilters({
      q: "",
      location: "",
      category: "",
      page: 1,
      page_size: 20,
    });
    setDateFrom(undefined);
    setDateTo(undefined);
  };

  const handlePageChange = (newPage: number) => {
    setFilters(prev => ({ ...prev, page: newPage }));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>Annonces — Objets retrouvés | Retrouv'Tout</title>
        <meta 
          name="description" 
          content="Parcourez les annonces d'objets retrouvés et filtrez par lieu, date, catégorie et mot-clé." 
        />
        <link 
          rel="canonical" 
          href={typeof window !== "undefined" ? window.location.href : "/annonces"} 
        />
      </Helmet>

      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold">Annonces d'objets retrouvés</h1>
        <Button asChild variant="hero" size="sm">
          <a href="/poster">Publier une annonce</a>
        </Button>
      </div>

      {/* Filtres de recherche */}
      <Card className="mb-6">
        <CardContent className="pt-6">
          <div className="grid gap-3 md:grid-cols-4 mb-4">
            <div className="flex items-center gap-2 md:col-span-1">
              <Search className="text-muted-foreground" size={16} />
              <Input 
                placeholder="Mot-clé" 
                aria-label="Mot-clé"
                value={filters.q}
                onChange={(e) => setFilters(prev => ({ ...prev, q: e.target.value }))}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
            
            <div className="flex items-center gap-2 md:col-span-1">
              <MapPin className="text-muted-foreground" size={16} />
              <Input 
                placeholder="Lieu" 
                aria-label="Lieu"
                value={filters.location}
                onChange={(e) => setFilters(prev => ({ ...prev, location: e.target.value }))}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
            
            <div className="md:col-span-1">
              <Select 
                value={filters.category} 
                onValueChange={(value) => setFilters(prev => ({ ...prev, category: value }))}
              >
                <SelectTrigger aria-label="Catégorie">
                  <SelectValue placeholder="Catégorie" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">Toutes les catégories</SelectItem>
                  <SelectItem value="cles">Clés</SelectItem>
                  <SelectItem value="electronique">Électronique</SelectItem>
                  <SelectItem value="bagagerie">Bagagerie</SelectItem>
                  <SelectItem value="documents">Documents</SelectItem>
                  <SelectItem value="vetements">Vêtements</SelectItem>
                  <SelectItem value="autre">Autre</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <div className="md:col-span-1">
              <Popover>
                <PopoverTrigger asChild>
                  <Button 
                    variant="outline" 
                    className={cn(
                      "w-full justify-start text-left font-normal", 
                      !dateFrom && !dateTo && "text-muted-foreground"
                    )}
                  >                    
                    <CalendarIcon className="mr-2" size={16} />
                    {dateFrom || dateTo ? (
                      `${dateFrom ? format(dateFrom, "dd/MM") : "..."} - ${dateTo ? format(dateTo, "dd/MM") : "..."}`
                    ) : (
                      "Période"
                    )}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                  <div className="p-3 space-y-3">
                    <div>
                      <label className="text-sm font-medium">Date de début</label>
                      <Calendar 
                        mode="single" 
                        selected={dateFrom} 
                        onSelect={setDateFrom} 
                        locale={fr}
                        disabled={(date) => date > new Date()}
                      />
                    </div>
                    <div>
                      <label className="text-sm font-medium">Date de fin</label>
                      <Calendar 
                        mode="single" 
                        selected={dateTo} 
                        onSelect={setDateTo} 
                        locale={fr}
                        disabled={(date) => date > new Date() || (dateFrom && date < dateFrom)}
                      />
                    </div>
                  </div>
                </PopoverContent>
              </Popover>
            </div>
          </div>

          <div className="flex gap-2">
            <Button onClick={handleSearch} variant="hero" size="sm">
              <Search className="mr-2" size={16} />
              Rechercher
            </Button>
            <Button onClick={handleResetFilters} variant="outline" size="sm">
              Réinitialiser
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Résultats */}
      {error && (
        <Card className="mb-6">
          <CardContent className="pt-6 text-center">
            <p className="text-destructive">
              Erreur lors du chargement des annonces. Veuillez réessayer.
            </p>
          </CardContent>
        </Card>
      )}

      {isLoading ? (
        <div className="text-center py-10">
          <Loader2 className="mx-auto h-8 w-8 animate-spin" />
          <p className="mt-4 text-muted-foreground">Chargement des annonces...</p>
        </div>
      ) : listingsData?.items?.length ? (
        <>
          <div className="flex items-center justify-between mb-4">
            <p className="text-sm text-muted-foreground">
              {listingsData.total} annonce{listingsData.total > 1 ? 's' : ''} trouvée{listingsData.total > 1 ? 's' : ''}
            </p>
          </div>
          
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {listingsData.items.map((item) => (
              <ListingCard key={item.id} item={item} />
            ))}
          </div>

          {/* Pagination */}
          {listingsData.totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-8">
              <Button
                variant="outline"
                onClick={() => handlePageChange(filters.page! - 1)}
                disabled={filters.page === 1}
              >
                Précédent
              </Button>
              
              <div className="flex items-center gap-1">
                {Array.from({ length: Math.min(5, listingsData.totalPages) }, (_, i) => {
                  const pageNum = Math.max(1, Math.min(
                    listingsData.totalPages - 4,
                    filters.page! - 2
                  )) + i;
                  
                  return (
                    <Button
                      key={pageNum}
                      variant={pageNum === filters.page ? "default" : "outline"}
                      size="sm"
                      onClick={() => handlePageChange(pageNum)}
                    >
                      {pageNum}
                    </Button>
                  );
                })}
              </div>

              <Button
                variant="outline"
                onClick={() => handlePageChange(filters.page! + 1)}
                disabled={filters.page === listingsData.totalPages}
              >
                Suivant
              </Button>
            </div>
          )}
        </>
      ) : (
        <Card>
          <CardContent className="pt-6 text-center py-10">
            <Search className="mx-auto h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">Aucune annonce trouvée</h3>
            <p className="text-muted-foreground mb-4">
              Essayez de modifier vos critères de recherche ou{" "}
              <button 
                onClick={handleResetFilters}
                className="text-primary hover:underline"
              >
                réinitialisez les filtres
              </button>
            </p>
            <Button asChild variant="hero">
              <a href="/poster">Publier la première annonce</a>
            </Button>
          </CardContent>
        </Card>
      )}
    </main>
  );
};

export default Annonces;
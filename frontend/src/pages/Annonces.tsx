import { Helmet } from "react-helmet-async";
import { Input } from "@/components/ui/input";
import { Select, SelectTrigger, SelectContent, SelectItem, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { format } from "date-fns";
import { fr } from "date-fns/locale";
import { Search, Calendar as CalendarIcon, MapPin } from "lucide-react";
import ListingCard from "@/components/listings/ListingCard";
import { listings } from "@/data/listings";

const Annonces = () => {
  const [date, setDate] = useState<Date | undefined>(undefined);

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>Annonces — Objets retrouvés | Retrouv’Tout</title>
        <meta name="description" content="Parcourez les annonces d’objets retrouvés et filtrez par lieu, date, catégorie et mot-clé." />
        <link rel="canonical" href={typeof window !== "undefined" ? window.location.href : "/annonces"} />
      </Helmet>

      <h1 className="text-3xl font-bold mb-6">Annonces d’objets retrouvés</h1>

      <div className="grid gap-3 md:grid-cols-4 mb-6">
        <div className="flex items-center gap-2 md:col-span-1">
          <Search className="text-muted-foreground" />
          <Input placeholder="Mot-clé" aria-label="Mot-clé" />
        </div>
        <div className="flex items-center gap-2 md:col-span-1">
          <MapPin className="text-muted-foreground" />
          <Input placeholder="Lieu" aria-label="Lieu" />
        </div>
        <div className="md:col-span-1">
          <Select>
            <SelectTrigger aria-label="Catégorie"><SelectValue placeholder="Catégorie" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="cles">Clés</SelectItem>
              <SelectItem value="electronique">Électronique</SelectItem>
              <SelectItem value="bagagerie">Bagagerie</SelectItem>
              <SelectItem value="documents">Documents</SelectItem>
              <SelectItem value="autre">Autre</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div className="md:col-span-1">
          <Popover>
            <PopoverTrigger asChild>
              <Button variant="outline" className={cn("w-full justify-start text-left font-normal", !date && "text-muted-foreground")}>                    
                <CalendarIcon className="mr-2" />
                {date ? format(date, "PPP", { locale: fr }) : "Date"}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0" align="start">
              <Calendar mode="single" selected={date} onSelect={setDate} initialFocus locale={fr} />
            </PopoverContent>
          </Popover>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        {listings.map((item) => (
          <ListingCard key={item.id} item={item} />
        ))}
      </div>
    </main>
  );
};

export default Annonces;

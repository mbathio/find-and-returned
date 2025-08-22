// src/components/listings/ListingCard.tsx - VERSION CORRIGÉE
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Listing } from "@/services/listings";
import { format, parseISO } from "date-fns";
import { fr } from "date-fns/locale";
import { Link } from "react-router-dom";
import { MapPin, Calendar, User } from "lucide-react";

interface Props { 
  item: Listing;
}

const getCategoryLabel = (category: string): string => {
  const categoryMap: Record<string, string> = {
    "cles": "Clés",
    "electronique": "Électronique", 
    "bagagerie": "Bagagerie",
    "documents": "Documents",
    "vetements": "Vêtements",
    "autre": "Autre"
  };
  return categoryMap[category] || category;
};

const getCategoryColor = (category: string): string => {
  const colorMap: Record<string, string> = {
    "cles": "bg-blue-100 text-blue-800",
    "electronique": "bg-purple-100 text-purple-800",
    "bagagerie": "bg-green-100 text-green-800", 
    "documents": "bg-orange-100 text-orange-800",
    "vetements": "bg-pink-100 text-pink-800",
    "autre": "bg-gray-100 text-gray-800"
  };
  return colorMap[category] || "bg-gray-100 text-gray-800";
};

const ListingCard = ({ item }: Props) => {
  const foundAtDate = parseISO(item.foundAt);
  const createdAtDate = parseISO(item.createdAt);

  return (
    <Card className="overflow-hidden group hover:shadow-lg transition-all duration-200">
      <div className="relative">
        <img
          src={item.imageUrl || "/placeholder.svg"}
          alt={`Objet retrouvé: ${item.title}`}
          loading="lazy"
          className="h-40 w-full object-cover"
        />
        <div className="absolute top-2 right-2">
          <Badge className={getCategoryColor(item.category)}>
            {getCategoryLabel(item.category)}
          </Badge>
        </div>
        {item.status === "resolved" && (
          <div className="absolute top-2 left-2">
            <Badge variant="secondary">Résolu</Badge>
          </div>
        )}
      </div>

      <CardContent className="pt-4">
        <div className="space-y-3">
          {/* Titre */}
          <h3 className="text-lg font-semibold line-clamp-2 group-hover:text-primary transition-colors">
            {item.title}
          </h3>
          
          {/* Informations de lieu et date */}
          <div className="space-y-1 text-sm text-muted-foreground">
            <div className="flex items-center gap-1">
              <MapPin className="h-3 w-3 flex-shrink-0" />
              <span className="truncate">{item.locationText}</span>
            </div>
            <div className="flex items-center gap-1">
              <Calendar className="h-3 w-3 flex-shrink-0" />
              <span>Trouvé le {format(foundAtDate, "d MMMM yyyy", { locale: fr })}</span>
            </div>
          </div>

          {/* Description */}
          <p className="text-sm line-clamp-2 leading-relaxed">
            {item.description}
          </p>

          {/* Métadonnées */}
          <div className="flex items-center justify-between text-xs text-muted-foreground">
            <div className="flex items-center gap-1">
              <User className="h-3 w-3" />
              <span>ID: {item.finderUserId.slice(-6)}</span>
            </div>
            <span>
              Publié {format(createdAtDate, "d MMM", { locale: fr })}
            </span>
          </div>

          {/* Actions */}
          <div className="flex gap-2 pt-2">
            <Button asChild variant="default" size="sm" className="flex-1">
              <Link to={`/annonces/${item.id}`}>
                Voir détails
              </Link>
            </Button>
            <Button asChild variant="outline" size="sm">
              <Link to={`/messages?listing=${item.id}`}>
                Contacter
              </Link>
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default ListingCard;
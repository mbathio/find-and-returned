import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Listing } from "@/data/listings";
import { format } from "date-fns";
import { fr } from "date-fns/locale";
import { Link } from "react-router-dom";

interface Props { item: Listing }

const ListingCard = ({ item }: Props) => {
  return (
    <Card className="overflow-hidden group">
      <img
        src={item.image || "/placeholder.svg"}
        alt={`Objet retrouvé: ${item.title}`}
        loading="lazy"
        className="h-40 w-full object-cover"
      />
      <CardContent className="pt-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">{item.title}</h3>
          <span className="text-xs rounded bg-secondary px-2 py-1 text-secondary-foreground">{item.category}</span>
        </div>
        <p className="text-sm text-muted-foreground mt-1">{item.location} • {format(new Date(item.date), "PPP", { locale: fr })}</p>
        <p className="mt-3 text-sm line-clamp-2">{item.description}</p>
        <div className="mt-4 flex gap-2">
          <Button asChild variant="soft" size="sm"><Link to="/messages">Contacter</Link></Button>
          <Button asChild variant="outline" size="sm"><Link to={`/annonces/${item.id}`}>Détails</Link></Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default ListingCard;

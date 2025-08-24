import { Helmet } from "react-helmet-async";
import { useParams, Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { format } from "date-fns";
import { fr } from "date-fns/locale";
import { useEffect, useState } from "react";

interface Listing {
  id: string;
  title: string;
  category: string;
  locationText: string;
  description: string;
  imageUrl: string | null;
  foundAt: string; // ISO string
}

const AnnonceDetail = () => {
  const { id } = useParams<{ id: string }>();
  const [item, setItem] = useState<Listing | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    const fetchAnnonce = async () => {
      try {
        const res = await fetch(`http://localhost:8081/api/listings/${id}`);

        // Si la réponse n'est pas JSON, considérer comme erreur
        const contentType = res.headers.get("content-type");
        if (!contentType || !contentType.includes("application/json")) {
          setError(true);
          setLoading(false);
          return;
        }

        const data: Listing = await res.json();

        if (!data || !data.id) {
          setError(true);
        } else {
          setItem(data);
        }
      } catch (err) {
        console.error("Erreur fetch annonce:", err);
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    fetchAnnonce();
  }, [id]);

  if (loading) {
    return (
      <main className="min-h-[60vh] container mx-auto py-10 grid place-items-center">
        <p>Chargement de l'annonce…</p>
      </main>
    );
  }

  if (error || !item) {
    return (
      <main className="min-h-[60vh] container mx-auto py-10 grid place-items-center">
        <div className="text-center">
          <h1 className="text-3xl font-bold mb-2">Annonce introuvable</h1>
          <p className="text-muted-foreground mb-4">
            Cette annonce n’existe pas ou a été supprimée.
          </p>
          <Button asChild variant="hero">
            <Link to="/annonces">Retour aux annonces</Link>
          </Button>
        </div>
      </main>
    );
  }

  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "Article",
    name: item.title,
    description: item.description,
    about: "Objet retrouvé",
    datePublished: item.foundAt,
    articleSection: item.category,
    contentLocation: item.locationText,
  };

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>{`${item.title} — ${item.locationText} | Retrouv’Tout`}</title>
        <meta
          name="description"
          content={`${item.category} trouvé à ${item.locationText} le ${format(
            new Date(item.foundAt),
            "PPP",
            { locale: fr }
          )}. ${item.description}`}
        />
        <script type="application/ld+json">{JSON.stringify(jsonLd)}</script>
        <link
          rel="canonical"
          href={
            typeof window !== "undefined"
              ? window.location.href
              : `/annonces/${item.id}`
          }
        />
      </Helmet>

      <article className="grid gap-6 md:grid-cols-3">
        <div className="md:col-span-2">
          <img
            src={item.imageUrl || "/placeholder.svg"}
            alt={`Objet retrouvé: ${item.title}`}
            className="w-full h-72 object-cover rounded-lg border"
          />
          <h1 className="text-3xl font-bold mt-4">{item.title}</h1>
          <p className="text-muted-foreground mt-1">
            {item.locationText} •{" "}
            {format(new Date(item.foundAt), "PPP", { locale: fr })} •{" "}
            {item.category}
          </p>
          <p className="mt-4 text-base leading-7">{item.description}</p>
        </div>
        <aside className="md:col-span-1">
          <Card>
            <CardContent className="pt-6">
              <h2 className="text-lg font-semibold mb-2">Entrer en contact</h2>
              <p className="text-sm text-muted-foreground mb-4">
                Vos coordonnées restent masquées jusqu’à accord mutuel.
              </p>
              <div className="flex flex-col gap-2">
                <Button asChild variant="hero">
                  <Link to="/messages">Envoyer un message</Link>
                </Button>
                <Button asChild variant="outline">
                  <Link to="/profil">Activer les notifications</Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        </aside>
      </article>
    </main>
  );
};

export default AnnonceDetail;

import { Helmet } from "react-helmet-async";
import { useParams, Link } from "react-router-dom";
import { listings } from "@/data/listings";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { format } from "date-fns";
import { fr } from "date-fns/locale";

const AnnonceDetail = () => {
  const { id } = useParams();
  const item = listings.find((l) => l.id === id);

  if (!item) {
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
    datePublished: item.date,
    articleSection: item.category,
    contentLocation: item.location,
  };

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>{`${item.title} — ${item.location} | Retrouv’Tout`}</title>
        <meta
          name="description"
          content={`${item.category} trouvé à ${item.location} le ${format(
            new Date(item.date),
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
            src={item.image || "/placeholder.svg"}
            alt={`Objet retrouvé: ${item.title}`}
            className="w-full h-72 object-cover rounded-lg border"
          />
          <h1 className="text-3xl font-bold mt-4">{item.title}</h1>
          <p className="text-muted-foreground mt-1">
            {item.location} •{" "}
            {format(new Date(item.date), "PPP", { locale: fr })} •{" "}
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

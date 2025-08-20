import { Helmet } from "react-helmet-async";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectTrigger,
  SelectContent,
  SelectItem,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent } from "@/components/ui/card";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { toast } from "@/hooks/use-toast";
import { KeyRound, MapPin, Search, Mail } from "lucide-react";

const Index = () => {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState("");
  const [location, setLocation] = useState("");
  const [category, setCategory] = useState<string | undefined>(undefined);

  const onSearch = (e: React.FormEvent) => {
    e.preventDefault();
    toast({
      title: "Recherche démo",
      description:
        "Connectez Supabase pour activer la recherche en temps réel.",
    });
    navigate("/annonces");
  };

  return (
    <main>
      <Helmet>
        <title>Retrouv'Tout — Objets perdus et retrouvés près de vous</title>
        <meta
          name="description"
          content="Publiez, recherchez et contactez en toute sécurité. Retrouv'Tout facilite la retrouvaille d'objets perdus."
        />
        <link
          rel="canonical"
          href={typeof window !== "undefined" ? window.location.href : "/"}
        />
      </Helmet>

      <section className="relative overflow-hidden">
        <div className="container mx-auto grid gap-8 py-14 md:grid-cols-2">
          <div className="flex flex-col justify-center">
            <h1 className="text-4xl md:text-5xl font-bold tracking-tight mb-4">
              Retrouv'Tout — Objets perdus et retrouvés
            </h1>
            <p className="text-lg text-muted-foreground mb-6">
              Une plateforme simple pour publier les objets retrouvés,
              rechercher vos objets perdus, et discuter en toute sécurité.
            </p>

            <form
              onSubmit={onSearch}
              className="grid gap-3 md:grid-cols-3 md:items-center"
            >
              <div className="md:col-span-1">
                <div className="flex items-center gap-2">
                  <Search className="text-muted-foreground" />
                  <Input
                    placeholder="Mot-clé (ex: portefeuille)"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    aria-label="Mot-clé"
                  />
                </div>
              </div>
              <div className="md:col-span-1">
                <div className="flex items-center gap-2">
                  <MapPin className="text-muted-foreground" />
                  <Input
                    placeholder="Lieu (ville, arrêt...)"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    aria-label="Lieu"
                  />
                </div>
              </div>
              <div className="md:col-span-1">
                <Select onValueChange={setCategory}>
                  <SelectTrigger aria-label="Catégorie">
                    <SelectValue placeholder="Catégorie" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="cles">Clés</SelectItem>
                    <SelectItem value="electronique">Électronique</SelectItem>
                    <SelectItem value="bagagerie">Bagagerie</SelectItem>
                    <SelectItem value="documents">Documents</SelectItem>
                    <SelectItem value="autre">Autre</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="md:col-span-3 flex gap-3">
                <Button type="submit" variant="hero">
                  Rechercher
                </Button>
                <Button variant="soft" asChild>
                  <a href="/poster">J'ai retrouvé un objet</a>
                </Button>
              </div>
            </form>
          </div>

          <div className="relative">
            <img
              src="/téléchargement.jpg"
              alt="Illustration Retrouv'Tout, objets perdus et retrouvés"
              loading="lazy"
              className="w-full h-auto rounded-lg border"
            />
            <div className="pointer-events-none absolute -inset-10 -z-10 bg-gradient-to-br from-primary/10 to-primary/0 blur-2xl" />
          </div>
        </div>
      </section>

      <section className="container mx-auto py-10 grid gap-6 md:grid-cols-3">
        <Card>
          <CardContent className="pt-6">
            <div className="mb-2 flex items-center gap-2">
              <KeyRound className="text-primary" />
              <h2 className="text-xl font-semibold">Publiez en 1 minute</h2>
            </div>
            <p className="text-muted-foreground">
              Ajoutez une photo, un lieu et une date. Nous notifierons les
              personnes concernées.
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="mb-2 flex items-center gap-2">
              <Search className="text-primary" />
              <h2 className="text-xl font-semibold">Recherchez efficacement</h2>
            </div>
            <p className="text-muted-foreground">
              Filtrez par lieu, date, catégorie et mot-clé pour retrouver
              rapidement.
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="mb-2 flex items-center gap-2">
              <Mail className="text-primary" />
              <h2 className="text-xl font-semibold">Échange sécurisé</h2>
            </div>
            <p className="text-muted-foreground">
              Messagerie privée, coordonnées masquées jusqu'à validation des
              deux parties.
            </p>
          </CardContent>
        </Card>
      </section>

      <section className="container mx-auto pb-16">
        <div className="rounded-xl border p-6 md:p-8">
          <h2 className="text-2xl font-semibold mb-2">
            Contact rapide et vérifiable
          </h2>
          <p className="text-muted-foreground mb-4">
            Activez les notifications email/SMS pour être alerté dès qu'un objet
            correspond à votre recherche.
          </p>
          <div className="flex gap-3">
            <Button asChild variant="soft">
              <a href="/auth">Créer un compte</a>
            </Button>
            <Button asChild variant="outline">
              <a href="/annonces">Consulter les annonces</a>
            </Button>
          </div>
        </div>
      </section>
    </main>
  );
};

export default Index;

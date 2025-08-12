import { Helmet } from "react-helmet-async";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "@/hooks/use-toast";

const Profil = () => {
  const onSave = (e: React.FormEvent) => {
    e.preventDefault();
    toast({ title: "Profil démo", description: "Connectez Supabase pour sauvegarder votre profil et vos rôles." });
  };

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>Mon profil | Retrouv’Tout</title>
        <meta name="description" content="Gérez vos informations personnelles et choisissez votre rôle: Retrouveur ou Propriétaire." />
        <link rel="canonical" href={typeof window !== "undefined" ? window.location.href : "/profil"} />
      </Helmet>

      <h1 className="text-3xl font-bold mb-6">Mon profil</h1>

      <Card className="max-w-2xl">
        <CardContent className="pt-6">
          <form onSubmit={onSave} className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-medium">Nom</label>
              <Input placeholder="Votre nom" required />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium">Email</label>
              <Input type="email" placeholder="vous@exemple.com" required />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium">Téléphone</label>
              <Input placeholder="06 12 34 56 78" />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium">Rôle</label>
              <Select>
                <SelectTrigger><SelectValue placeholder="Sélectionnez" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="retrouveur">Retrouveur</SelectItem>
                  <SelectItem value="proprietaire">Propriétaire</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="md:col-span-2 flex justify-end gap-3">
              <Button type="button" variant="outline">Annuler</Button>
              <Button type="submit" variant="hero">Enregistrer</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </main>
  );
};

export default Profil;

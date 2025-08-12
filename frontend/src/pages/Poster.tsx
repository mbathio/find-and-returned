import { Helmet } from "react-helmet-async";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectTrigger, SelectContent, SelectItem, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { format } from "date-fns";
import { fr } from "date-fns/locale";
import { Calendar as CalendarIcon, Upload } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { toast } from "@/hooks/use-toast";

const Poster = () => {
  const [date, setDate] = useState<Date | undefined>(new Date());
  const [preview, setPreview] = useState<string | undefined>(undefined);

  const onFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const url = URL.createObjectURL(file);
      setPreview(url);
    }
  };

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    toast({ title: "Publication démo", description: "Connectez Supabase pour enregistrer l’annonce et la photo." });
  };

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>Publier un objet retrouvé | Retrouv’Tout</title>
        <meta name="description" content="Ajoutez rapidement une annonce avec photo, lieu, date, description et catégorie." />
        <link rel="canonical" href={typeof window !== "undefined" ? window.location.href : "/poster"} />
      </Helmet>

      <h1 className="text-3xl font-bold mb-6">Publier un objet retrouvé</h1>

      <Card>
        <CardContent className="pt-6">
          <form onSubmit={onSubmit} className="grid gap-4 md:grid-cols-2">
            <div className="grid gap-4">
              <div>
                <label className="mb-2 block text-sm font-medium">Type d’objet</label>
                <Input placeholder="Ex: clés Opel, iPhone 13…" required />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">Catégorie</label>
                <Select>
                  <SelectTrigger><SelectValue placeholder="Sélectionnez" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="cles">Clés</SelectItem>
                    <SelectItem value="electronique">Électronique</SelectItem>
                    <SelectItem value="bagagerie">Bagagerie</SelectItem>
                    <SelectItem value="documents">Documents</SelectItem>
                    <SelectItem value="autre">Autre</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">Lieu de découverte</label>
                <Input placeholder="Ville, arrêt, lieu précis" required />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">Date</label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button variant="outline" className="justify-start">
                      <CalendarIcon className="mr-2" />
                      {date ? format(date, "PPP", { locale: fr }) : "Choisir une date"}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar mode="single" selected={date} onSelect={setDate} initialFocus locale={fr} />
                  </PopoverContent>
                </Popover>
              </div>
            </div>
            <div className="grid gap-4">
              <div>
                <label className="mb-2 block text-sm font-medium">Photo</label>
                <Input type="file" accept="image/*" onChange={onFile} />
                {preview && (
                  <img src={preview} alt="Prévisualisation de l’objet retrouvé" className="mt-3 h-48 w-full rounded-md object-cover border" />
                )}
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">Description</label>
                <Textarea placeholder="Détails utiles (couleurs, marques, particularités)" rows={6} />
              </div>
            </div>
            <div className="md:col-span-2 flex justify-end gap-3">
              <Button type="button" variant="outline">Annuler</Button>
              <Button type="submit" variant="hero"><Upload className="mr-2" />Publier</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </main>
  );
};

export default Poster;

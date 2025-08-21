import { Helmet } from "react-helmet-async";
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectTrigger,
  SelectContent,
  SelectItem,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { format } from "date-fns";
import { fr } from "date-fns/locale";
import { Calendar as CalendarIcon, Upload } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { toast } from "@/hooks/use-toast";

const Poster = () => {
  const [date, setDate] = useState<Date | undefined>(new Date());
  const [preview, setPreview] = useState<string | undefined>(undefined);
  const [formData, setFormData] = useState({
    typeObjet: "",
    categorie: "",
    lieu: "",
    description: "",
  });

  const onFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const url = URL.createObjectURL(file);
      setPreview(url);
    }
  };

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Validation des champs requis du cahier des charges
    if (!formData.typeObjet || !formData.categorie || !formData.lieu || !date) {
      toast({
        title: "Champs manquants",
        description: "Veuillez remplir tous les champs obligatoires.",
        variant: "destructive",
      });
      return;
    }

    toast({
      title: "Publication démo",
      description: "Connectez Supabase pour enregistrer l'annonce et la photo.",
    });
  };

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>Publier un objet retrouvé | Retrouv'Tout</title>
        <meta
          name="description"
          content="Publiez une annonce d'objet retrouvé avec photo, lieu, date et description."
        />
        <link
          rel="canonical"
          href={
            typeof window !== "undefined" ? window.location.href : "/poster"
          }
        />
      </Helmet>

      <h1 className="text-3xl font-bold mb-6">Publier un objet retrouvé</h1>

      <Card>
        <CardContent className="pt-6">
          <form onSubmit={onSubmit} className="grid gap-4 md:grid-cols-2">
            <div className="grid gap-4">
              {/* Type d'objet - Requis par le cahier des charges */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Type d'objet <span className="text-red-500">*</span>
                </label>
                <Input
                  placeholder="Ex: clés Opel, iPhone 13…"
                  value={formData.typeObjet}
                  onChange={(e) =>
                    setFormData({ ...formData, typeObjet: e.target.value })
                  }
                  required
                />
              </div>

              {/* Catégorie - Toutes les catégories conservées */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Catégorie <span className="text-red-500">*</span>
                </label>
                <Select
                  value={formData.categorie}
                  onValueChange={(value) =>
                    setFormData({ ...formData, categorie: value })
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Sélectionnez une catégorie" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="cles">Clés</SelectItem>
                    <SelectItem value="electronique">Électronique</SelectItem>
                    <SelectItem value="bagagerie">Bagagerie</SelectItem>
                    <SelectItem value="documents">Documents</SelectItem>
                    <SelectItem value="vetements">Vêtements</SelectItem>
                    <SelectItem value="autre">Autre</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Lieu de découverte - Requis par le cahier des charges */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Lieu de découverte <span className="text-red-500">*</span>
                </label>
                <Input
                  placeholder="Ville, arrêt, lieu précis"
                  value={formData.lieu}
                  onChange={(e) =>
                    setFormData({ ...formData, lieu: e.target.value })
                  }
                  required
                />
              </div>

              {/* Date - Requis par le cahier des charges */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Date de découverte <span className="text-red-500">*</span>
                </label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button variant="outline" className="justify-start">
                      <CalendarIcon className="mr-2" size={16} />
                      {date
                        ? format(date, "PPP", { locale: fr })
                        : "Choisir une date"}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={date}
                      onSelect={setDate}
                      initialFocus
                      locale={fr}
                    />
                  </PopoverContent>
                </Popover>
              </div>
            </div>

            <div className="grid gap-4">
              {/* Photo - Optionnel mais recommandé */}
              <div>
                <label className="mb-2 block text-sm font-medium">Photo</label>
                <Input type="file" accept="image/*" onChange={onFile} />
                {preview && (
                  <img
                    src={preview}
                    alt="Prévisualisation de l'objet retrouvé"
                    className="mt-3 h-48 w-full rounded-md object-cover border"
                  />
                )}
              </div>

              {/* Description - Requis par le cahier des charges */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Description <span className="text-red-500">*</span>
                </label>
                <Textarea
                  placeholder="Détails utiles (couleurs, marques, particularités)"
                  value={formData.description}
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
                  rows={6}
                  required
                />
              </div>
            </div>

            <div className="md:col-span-2 flex justify-end gap-3">
              <Button type="button" variant="outline">
                Annuler
              </Button>
              <Button type="submit" variant="hero">
                <Upload className="mr-2" size={16} />
                Publier
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </main>
  );
};

export default Poster;

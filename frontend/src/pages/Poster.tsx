// src/pages/Poster.tsx - VERSION CORRIGÉE
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
import { Calendar as CalendarIcon, Upload, Loader2 } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { toast } from "@/hooks/use-toast";
import { useCreateListing, useUploadImage, CreateListingRequest } from "@/services/listings";
import { useNavigate } from "react-router-dom";
import { authService } from "@/services/auth";

const Poster = () => {
  const navigate = useNavigate();
  const createListingMutation = useCreateListing();
  const uploadImageMutation = useUploadImage();

  const [date, setDate] = useState<Date | undefined>(new Date());
  const [preview, setPreview] = useState<string | undefined>(undefined);
  const [selectedFile, setSelectedFile] = useState<File | undefined>(undefined);
  const [uploadProgress, setUploadProgress] = useState<number>(0);

  const [formData, setFormData] = useState({
    title: "",
    category: "",
    locationText: "",
    latitude: undefined as number | undefined,
    longitude: undefined as number | undefined,
    description: "",
  });

  // Vérifier l'authentification
  const isAuthenticated = authService.isAuthenticated();

  const onFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // Validation du fichier
      const maxSize = 10 * 1024 * 1024; // 10MB
      const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];

      if (file.size > maxSize) {
        toast({
          title: "Fichier trop volumineux",
          description: "La taille maximale autorisée est de 10MB.",
          variant: "destructive",
        });
        return;
      }

      if (!allowedTypes.includes(file.type)) {
        toast({
          title: "Type de fichier non autorisé",
          description: "Seuls les formats JPG, PNG, GIF et WebP sont acceptés.",
          variant: "destructive",
        });
        return;
      }

      setSelectedFile(file);
      const url = URL.createObjectURL(file);
      setPreview(url);
    }
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!isAuthenticated) {
      toast({
        title: "Connexion requise",
        description: "Vous devez être connecté pour publier une annonce.",
        variant: "destructive",
      });
      navigate("/auth?redirect=/poster");
      return;
    }

    // Validation des champs requis
    if (!formData.title || !formData.category || !formData.locationText || !date || !formData.description) {
      toast({
        title: "Champs manquants",
        description: "Veuillez remplir tous les champs obligatoires.",
        variant: "destructive",
      });
      return;
    }

    try {
      let imageUrl: string | undefined = undefined;

      // Upload de l'image si présente
      if (selectedFile) {
        const uploadResult = await uploadImageMutation.mutateAsync({
          file: selectedFile,
          onProgress: setUploadProgress,
        });
        imageUrl = uploadResult.url;
      }

      // Préparation des données conformes au backend
      const listingData: CreateListingRequest = {
        title: formData.title,
        category: formData.category,
        locationText: formData.locationText,
        latitude: formData.latitude,
        longitude: formData.longitude,
        foundAt: date.toISOString(), // Format ISO pour LocalDateTime
        description: formData.description,
        imageUrl: imageUrl,
      };

      // Création de l'annonce
      const newListing = await createListingMutation.mutateAsync(listingData);

      toast({
        title: "Annonce publiée !",
        description: "Votre annonce a été publiée avec succès.",
      });

      // Redirection vers la page de l'annonce
      navigate(`/annonces/${newListing.id}`);

    } catch (error: any) {
      console.error("Erreur lors de la publication:", error);
      toast({
        title: "Erreur de publication",
        description: error.message || "Une erreur est survenue lors de la publication.",
        variant: "destructive",
      });
    }
  };

  const handleLocationDetection = () => {
    if ("geolocation" in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setFormData({
            ...formData,
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          });
          toast({
            title: "Position détectée",
            description: "Votre position a été ajoutée à l'annonce.",
          });
        },
        (error) => {
          console.error("Erreur de géolocalisation:", error);
          toast({
            title: "Géolocalisation indisponible",
            description: "Impossible de détecter votre position.",
            variant: "destructive",
          });
        }
      );
    }
  };

  if (!isAuthenticated) {
    return (
      <main className="container mx-auto py-10">
        <Card className="max-w-md mx-auto">
          <CardContent className="pt-6 text-center">
            <h2 className="text-xl font-semibold mb-4">Connexion requise</h2>
            <p className="text-muted-foreground mb-4">
              Vous devez être connecté pour publier une annonce.
            </p>
            <Button asChild variant="hero">
              <a href="/auth?redirect=/poster">Se connecter</a>
            </Button>
          </CardContent>
        </Card>
      </main>
    );
  }

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
                  value={formData.title}
                  onChange={(e) =>
                    setFormData({ ...formData, title: e.target.value })
                  }
                  required
                />
              </div>

              {/* Catégorie - Conforme au backend */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Catégorie <span className="text-red-500">*</span>
                </label>
                <Select
                  value={formData.category}
                  onValueChange={(value) =>
                    setFormData({ ...formData, category: value })
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

              {/* Lieu de découverte */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Lieu de découverte <span className="text-red-500">*</span>
                </label>
                <div className="flex gap-2">
                  <Input
                    placeholder="Ville, arrêt, lieu précis"
                    value={formData.locationText}
                    onChange={(e) =>
                      setFormData({ ...formData, locationText: e.target.value })
                    }
                    required
                    className="flex-1"
                  />
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleLocationDetection}
                  >
                    📍
                  </Button>
                </div>
                {formData.latitude && formData.longitude && (
                  <p className="text-xs text-muted-foreground mt-1">
                    Position GPS ajoutée
                  </p>
                )}
              </div>

              {/* Date de découverte */}
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
                      disabled={(date) => date > new Date() || date < new Date("2020-01-01")}
                    />
                  </PopoverContent>
                </Popover>
              </div>
            </div>

            <div className="grid gap-4">
              {/* Photo */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Photo de l'objet
                </label>
                <Input 
                  type="file" 
                  accept="image/jpeg,image/jpg,image/png,image/gif,image/webp" 
                  onChange={onFile}
                  disabled={uploadImageMutation.isPending}
                />
                {uploadImageMutation.isPending && (
                  <div className="mt-2">
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div 
                        className="bg-primary h-2 rounded-full transition-all duration-300" 
                        style={{ width: `${uploadProgress}%` }}
                      />
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      Upload en cours... {Math.round(uploadProgress)}%
                    </p>
                  </div>
                )}
                {preview && !uploadImageMutation.isPending && (
                  <img
                    src={preview}
                    alt="Prévisualisation de l'objet retrouvé"
                    className="mt-3 h-48 w-full rounded-md object-cover border"
                  />
                )}
              </div>

              {/* Description */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Description <span className="text-red-500">*</span>
                </label>
                <Textarea
                  placeholder="Détails utiles pour l'identification : couleurs, marques, particularités, état..."
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
              <Button 
                type="button" 
                variant="outline"
                onClick={() => navigate("/annonces")}
              >
                Annuler
              </Button>
              <Button 
                type="submit" 
                variant="hero"
                disabled={createListingMutation.isPending || uploadImageMutation.isPending}
              >
                {createListingMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Publication...
                  </>
                ) : (
                  <>
                    <Upload className="mr-2" size={16} />
                    Publier
                  </>
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </main>
  );
};

export default Poster;
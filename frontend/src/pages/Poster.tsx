// src/pages/Poster.tsx - VERSION FINALE CORRIGÉE POUR COMPATIBILITÉ BACKEND
import { Helmet } from "react-helmet-async";
import { useState, useTransition } from "react";
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
import {
  useCreateListing,
  useUploadImage,
  CreateListingRequest,
} from "@/services/listings";
import { useNavigate } from "react-router-dom";
import { authService } from "@/services/auth";

// ✅ FONCTION FORMATAGE DATE COMPATIBLE BACKEND LocalDateTime
const formatForBackendDateTime = (date: Date): string => {
  // Format exact attendu par Spring Boot LocalDateTime: YYYY-MM-DDTHH:mm:ss
  // SANS le 'Z' et SANS les millisecondes pour éviter tout problème de parsing
  return date.toISOString().slice(0, 19); // "2025-08-24T14:30:00"
};

const Poster = () => {
  const navigate = useNavigate();
  const [isPending, startTransition] = useTransition();
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
      const allowedTypes = [
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp",
      ];

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

      startTransition(() => {
        setSelectedFile(file);
        const url = URL.createObjectURL(file);
        setPreview(url);
      });
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
    if (
      !formData.title.trim() ||
      !formData.category ||
      !formData.locationText.trim() ||
      !date ||
      !formData.description.trim()
    ) {
      toast({
        title: "Champs manquants",
        description: "Veuillez remplir tous les champs obligatoires.",
        variant: "destructive",
      });
      return;
    }

    // ✅ VALIDATION SUPPLÉMENTAIRE : Longueurs max conformes au backend
    if (formData.title.length > 180) {
      toast({
        title: "Titre trop long",
        description: "Le titre ne peut pas dépasser 180 caractères.",
        variant: "destructive",
      });
      return;
    }

    if (formData.locationText.length > 255) {
      toast({
        title: "Lieu trop long",
        description: "Le lieu ne peut pas dépasser 255 caractères.",
        variant: "destructive",
      });
      return;
    }

    try {
      let imageUrl: string | undefined = undefined;

      // Upload de l'image si présente
      if (selectedFile) {
        toast({
          title: "Upload en cours",
          description: "Upload de l'image en cours...",
        });

        const uploadResult = await uploadImageMutation.mutateAsync({
          file: selectedFile,
          onProgress: setUploadProgress,
        });
        imageUrl = uploadResult.url;

        toast({
          title: "Image uploadée",
          description: "L'image a été uploadée avec succès.",
        });
      }

      // ✅ PRÉPARATION DES DONNÉES EXACTEMENT CONFORMES AU BACKEND
      const listingData: CreateListingRequest = {
        title: formData.title.trim(),
        category: formData.category, // "cles", "electronique", etc.
        locationText: formData.locationText.trim(),
        latitude: formData.latitude, // number | undefined → BigDecimal
        longitude: formData.longitude, // number | undefined → BigDecimal
        foundAt: formatForBackendDateTime(date), // ✅ Format parfait pour LocalDateTime
        description: formData.description.trim(),
        imageUrl: imageUrl, // string | undefined
      };

      console.log("📤 Données à envoyer au backend:", listingData);

      // Création de l'annonce
      const newListing = await createListingMutation.mutateAsync(listingData);

      toast({
        title: "Annonce publiée !",
        description: "Votre annonce a été publiée avec succès.",
      });

      // Redirection vers la page de l'annonce
      navigate(`/annonces/${newListing.id}`);
    } catch (error: unknown) {
      console.error("❌ Erreur lors de la publication:", error);

      // ✅ GESTION D'ERREUR AMÉLIORÉE
      let errorMessage = "Une erreur est survenue lors de la publication.";

      if (error && typeof error === "object") {
        if ("message" in error && typeof error.message === "string") {
          errorMessage = error.message;
        } else if (
          "response" in error &&
          error.response &&
          typeof error.response === "object"
        ) {
          interface ErrorResponse {
            data?: { message?: string };
            status?: number;
          }
          const response = error.response as ErrorResponse;
          if (response.data && response.data.message) {
            errorMessage = response.data.message;
          } else if (response.status === 401) {
            errorMessage = "Vous devez être connecté pour publier une annonce.";
            navigate("/auth?redirect=/poster");
            return;
          } else if (response.status === 400) {
            errorMessage = "Données invalides. Vérifiez les champs requis.";
          }
        }
      }

      toast({
        title: "Erreur de publication",
        description: errorMessage,
        variant: "destructive",
      });
    }
  };

  const handleLocationDetection = () => {
    if ("geolocation" in navigator) {
      toast({
        title: "Détection en cours",
        description: "Détection de votre position...",
      });

      navigator.geolocation.getCurrentPosition(
        (position) => {
          startTransition(() => {
            setFormData({
              ...formData,
              latitude: position.coords.latitude,
              longitude: position.coords.longitude,
            });
          });
          toast({
            title: "Position détectée",
            description: "Votre position GPS a été ajoutée à l'annonce.",
          });
        },
        (error) => {
          console.error("❌ Erreur de géolocalisation:", error);
          let errorMsg = "Impossible de détecter votre position.";

          switch (error.code) {
            case error.PERMISSION_DENIED:
              errorMsg =
                "Permission refusée. Autorisez la géolocalisation dans votre navigateur.";
              break;
            case error.POSITION_UNAVAILABLE:
              errorMsg = "Position non disponible.";
              break;
            case error.TIMEOUT:
              errorMsg = "Délai d'attente dépassé.";
              break;
          }

          toast({
            title: "Géolocalisation indisponible",
            description: errorMsg,
            variant: "destructive",
          });
        }
      );
    } else {
      toast({
        title: "Géolocalisation non supportée",
        description: "Votre navigateur ne supporte pas la géolocalisation.",
        variant: "destructive",
      });
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
              {/* Type d'objet - Requis */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Type d'objet <span className="text-red-500">*</span>
                </label>
                <Input
                  placeholder="Ex: clés Opel, iPhone 13, portefeuille cuir..."
                  value={formData.title}
                  onChange={(e) =>
                    setFormData({ ...formData, title: e.target.value })
                  }
                  required
                  maxLength={180}
                  aria-label="Type d'objet"
                />
                <p className="text-xs text-muted-foreground mt-1">
                  {formData.title.length}/180 caractères
                </p>
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
                    <SelectItem value="cles">🔑 Clés</SelectItem>
                    <SelectItem value="electronique">
                      📱 Électronique
                    </SelectItem>
                    <SelectItem value="bagagerie">🎒 Bagagerie</SelectItem>
                    <SelectItem value="documents">📄 Documents</SelectItem>
                    <SelectItem value="vetements">👕 Vêtements</SelectItem>
                    <SelectItem value="autre">🔍 Autre</SelectItem>
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
                    placeholder="Ex: Gare de Lyon, sortie 3, Metro Châtelet..."
                    value={formData.locationText}
                    onChange={(e) =>
                      setFormData({ ...formData, locationText: e.target.value })
                    }
                    required
                    maxLength={255}
                    className="flex-1"
                    aria-label="Lieu de découverte"
                  />
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleLocationDetection}
                    title="Détecter ma position GPS"
                  >
                    📍
                  </Button>
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {formData.locationText.length}/255 caractères
                  {formData.latitude && formData.longitude && (
                    <span className="text-green-600 ml-2">
                      • Position GPS ajoutée ({formData.latitude.toFixed(4)},{" "}
                      {formData.longitude.toFixed(4)})
                    </span>
                  )}
                </p>
              </div>

              {/* Date de découverte */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Date de découverte <span className="text-red-500">*</span>
                </label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button variant="outline" className="justify-start w-full">
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
                      disabled={(date) =>
                        date > new Date() || date < new Date("2020-01-01")
                      }
                    />
                  </PopoverContent>
                </Popover>
                <p className="text-xs text-muted-foreground mt-1">
                  Plus la date est précise, mieux c'est pour retrouver le
                  propriétaire
                </p>
              </div>
            </div>

            <div className="grid gap-4">
              {/* Photo */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Photo de l'objet (optionnel mais recommandé)
                </label>
                <Input
                  type="file"
                  accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                  onChange={onFile}
                  disabled={uploadImageMutation.isPending}
                  aria-label="Photo de l'objet"
                />
                <p className="text-xs text-muted-foreground mt-1">
                  Formats acceptés: JPG, PNG, GIF, WebP • Taille max: 10MB
                </p>

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
                  <div className="mt-3">
                    <img
                      src={preview}
                      alt="Prévisualisation de l'objet retrouvé"
                      className="h-48 w-full rounded-md object-cover border"
                    />
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      className="mt-2"
                      onClick={() => {
                        setPreview(undefined);
                        setSelectedFile(undefined);
                      }}
                    >
                      Supprimer la photo
                    </Button>
                  </div>
                )}
              </div>

              {/* Description */}
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Description <span className="text-red-500">*</span>
                </label>
                <Textarea
                  placeholder="Décrivez l'objet en détail : couleurs, marques, particularités, état, contenu visible, etc. Plus votre description est précise, plus il sera facile de retrouver le propriétaire."
                  value={formData.description}
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
                  rows={7}
                  required
                  aria-label="Description de l'objet"
                />
                <p className="text-xs text-muted-foreground mt-1">
                  💡 Évitez de mentionner des codes PIN, mots de passe ou
                  informations sensibles
                </p>
              </div>
            </div>

            <div className="md:col-span-2 flex justify-end gap-3 pt-4 border-t">
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
                disabled={
                  createListingMutation.isPending ||
                  uploadImageMutation.isPending ||
                  isPending ||
                  !formData.title.trim() ||
                  !formData.category ||
                  !formData.locationText.trim() ||
                  !date ||
                  !formData.description.trim()
                }
              >
                {createListingMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Publication en cours...
                  </>
                ) : (
                  <>
                    <Upload className="mr-2" size={16} />
                    Publier l'annonce
                  </>
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Aide et conseils */}
      <Card className="mt-6 border-l-4 border-l-primary">
        <CardContent className="pt-6">
          <h2 className="text-lg font-semibold mb-3 flex items-center">
            <span className="mr-2">💡</span>
            Conseils pour une annonce efficace
          </h2>
          <div className="grid gap-3 text-sm text-muted-foreground md:grid-cols-2">
            <div>
              <h3 className="font-medium text-foreground mb-2">📸 Photo</h3>
              <ul className="space-y-1">
                <li>• Prenez une photo nette et bien éclairée</li>
                <li>• Montrez l'objet dans son ensemble</li>
                <li>• Évitez les reflets et ombres</li>
              </ul>
            </div>
            <div>
              <h3 className="font-medium text-foreground mb-2">
                📝 Description
              </h3>
              <ul className="space-y-1">
                <li>• Couleurs, matériaux, taille</li>
                <li>• Marques, modèles visibles</li>
                <li>• État (neuf, usé, abîmé...)</li>
                <li>• Contenu visible (sans être indiscret)</li>
              </ul>
            </div>
          </div>
        </CardContent>
      </Card>
    </main>
  );
};

export default Poster;

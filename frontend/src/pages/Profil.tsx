// src/pages/Profil.tsx - VERSION CORRIGÉE COMPLÈTE AVEC MIXTE
import { Helmet } from "react-helmet-async";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "@/hooks/use-toast";
import { useAuth } from "@/contexts/AuthContext";
import { useState, useEffect } from "react";
import { User } from "@/services/auth";

const Profil = () => {
  const { user, isAuthenticated, updateUser } = useAuth();
  
  // États pour le formulaire
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    role: "mixte" as "retrouveur" | "proprietaire" | "mixte",
  });

  const [passwordData, setPasswordData] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  // Initialiser le formulaire avec les données utilisateur
  useEffect(() => {
    if (user) {
      setFormData({
        name: user.name || "",
        email: user.email || "",
        phone: user.phone || "",
        role: user.role || "mixte",
      });
    }
  }, [user]);

  const onSaveProfile = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validation basique
    if (!formData.name.trim()) {
      toast({
        title: "Nom requis",
        description: "Veuillez saisir votre nom.",
        variant: "destructive",
      });
      return;
    }

    if (!formData.email.trim()) {
      toast({
        title: "Email requis",
        description: "Veuillez saisir votre email.",
        variant: "destructive",
      });
      return;
    }

    // Simuler la sauvegarde (remplacer par un appel API réel)
    const updatedUser: User = {
      ...user!,
      name: formData.name.trim(),
      email: formData.email.trim(),
      phone: formData.phone.trim() || undefined,
      role: formData.role,
    };

    updateUser(updatedUser);
    
    toast({ 
      title: "Profil mis à jour", 
      description: "Vos informations ont été sauvegardées. (Démo - connectez votre API pour la sauvegarde réelle)" 
    });
  };

  const onChangePassword = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validation des mots de passe
    if (!passwordData.oldPassword) {
      toast({
        title: "Ancien mot de passe requis",
        description: "Veuillez saisir votre ancien mot de passe.",
        variant: "destructive",
      });
      return;
    }

    if (passwordData.newPassword.length < 6) {
      toast({
        title: "Mot de passe trop court",
        description: "Le nouveau mot de passe doit contenir au moins 6 caractères.",
        variant: "destructive",
      });
      return;
    }

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast({
        title: "Mots de passe différents",
        description: "La confirmation ne correspond pas au nouveau mot de passe.",
        variant: "destructive",
      });
      return;
    }

    // Réinitialiser le formulaire
    setPasswordData({
      oldPassword: "",
      newPassword: "",
      confirmPassword: "",
    });

    toast({ 
      title: "Mot de passe changé", 
      description: "Votre mot de passe a été mis à jour. (Démo - connectez votre API pour le changement réel)" 
    });
  };

  const getRoleLabel = (role: string) => {
    switch (role) {
      case "retrouveur":
        return "Retrouveur (je trouve des objets)";
      case "proprietaire":
        return "Propriétaire (je cherche mes objets)";
      case "mixte":
        return "Les deux (par défaut)";
      default:
        return "Utilisateur";
    }
  };

  if (!isAuthenticated) {
    return (
      <main className="container mx-auto py-10">
        <Card className="max-w-md mx-auto">
          <CardContent className="pt-6 text-center">
            <h2 className="text-xl font-semibold mb-4">Connexion requise</h2>
            <p className="text-muted-foreground mb-4">
              Vous devez être connecté pour accéder à votre profil.
            </p>
            <Button asChild variant="hero">
              <a href="/auth?redirect=/profil">Se connecter</a>
            </Button>
          </CardContent>
        </Card>
      </main>
    );
  }

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>Mon profil | Retrouv'Tout</title>
        <meta name="description" content="Gérez vos informations personnelles et choisissez votre rôle: Retrouveur, Propriétaire ou Mixte." />
        <link rel="canonical" href={typeof window !== "undefined" ? window.location.href : "/profil"} />
      </Helmet>

      <h1 className="text-3xl font-bold mb-6">Mon profil</h1>

      <div className="grid gap-6 max-w-4xl">
        {/* Informations personnelles */}
        <Card>
          <CardContent className="pt-6">
            <h2 className="text-xl font-semibold mb-4">Informations personnelles</h2>
            <form onSubmit={onSaveProfile} className="grid gap-4 md:grid-cols-2">
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Nom <span className="text-red-500">*</span>
                </label>
                <Input 
                  placeholder="Votre nom" 
                  required 
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Email <span className="text-red-500">*</span>
                </label>
                <Input 
                  type="email" 
                  placeholder="vous@exemple.com" 
                  required 
                  value={formData.email}
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">Téléphone</label>
                <Input 
                  placeholder="06 12 34 56 78" 
                  value={formData.phone}
                  onChange={(e) => setFormData({...formData, phone: e.target.value})}
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">Rôle</label>
                <Select 
                  value={formData.role}
                  onValueChange={(value: "retrouveur" | "proprietaire" | "mixte") => 
                    setFormData({...formData, role: value})
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Sélectionnez votre rôle" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="retrouveur">Retrouveur (je trouve des objets)</SelectItem>
                    <SelectItem value="proprietaire">Propriétaire (je cherche mes objets)</SelectItem>
                    <SelectItem value="mixte">Les deux (par défaut)</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-xs text-muted-foreground mt-1">
                  Rôle actuel : {getRoleLabel(formData.role)}
                </p>
              </div>
              <div className="md:col-span-2 flex justify-end gap-3">
                <Button 
                  type="button" 
                  variant="outline"
                  onClick={() => {
                    if (user) {
                      setFormData({
                        name: user.name || "",
                        email: user.email || "",
                        phone: user.phone || "",
                        role: user.role || "mixte",
                      });
                    }
                  }}
                >
                  Annuler
                </Button>
                <Button type="submit" variant="hero">Enregistrer</Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Changement de mot de passe */}
        <Card>
          <CardContent className="pt-6">
            <h2 className="text-xl font-semibold mb-4">Sécurité</h2>
            <form onSubmit={onChangePassword} className="grid gap-4 max-w-md">
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Ancien mot de passe <span className="text-red-500">*</span>
                </label>
                <Input 
                  type="password" 
                  placeholder="Mot de passe actuel"
                  value={passwordData.oldPassword}
                  onChange={(e) => setPasswordData({...passwordData, oldPassword: e.target.value})}
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Nouveau mot de passe <span className="text-red-500">*</span>
                </label>
                <Input 
                  type="password" 
                  placeholder="Nouveau mot de passe (min. 6 caractères)"
                  minLength={6}
                  value={passwordData.newPassword}
                  onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})}
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium">
                  Confirmer le nouveau mot de passe <span className="text-red-500">*</span>
                </label>
                <Input 
                  type="password" 
                  placeholder="Confirmez le nouveau mot de passe"
                  value={passwordData.confirmPassword}
                  onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})}
                />
              </div>
              <div className="flex justify-end gap-3">
                <Button 
                  type="button" 
                  variant="outline"
                  onClick={() => setPasswordData({
                    oldPassword: "",
                    newPassword: "",
                    confirmPassword: "",
                  })}
                >
                  Annuler
                </Button>
                <Button type="submit" variant="hero">Changer le mot de passe</Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Informations du compte */}
        <Card>
          <CardContent className="pt-6">
            <h2 className="text-xl font-semibold mb-4">Informations du compte</h2>
            <div className="grid gap-3 text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">ID utilisateur :</span>
                <span className="font-mono">{user?.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Email vérifié :</span>
                <span className={user?.email_verified ? "text-green-600" : "text-orange-600"}>
                  {user?.email_verified ? "✅ Oui" : "⚠️ Non"}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Compte créé :</span>
                <span>{user?.created_at ? new Date(user.created_at).toLocaleDateString("fr-FR") : "N/A"}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Dernière connexion :</span>
                <span>{user?.last_login_at ? new Date(user.last_login_at).toLocaleDateString("fr-FR") : "N/A"}</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </main>
  );
};

export default Profil;
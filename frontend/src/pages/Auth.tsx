// src/pages/Auth.tsx - VERSION CORRIGÉE COMPLÈTE AVEC MIXTE
import { Helmet } from "react-helmet-async";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "@/hooks/use-toast";
import { useLogin, useRegister, LoginRequest, RegisterRequest } from "@/services/auth";
import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

const Auth = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const redirectTo = searchParams.get("redirect") || "/";

  // États pour les formulaires
  const [loginData, setLoginData] = useState<LoginRequest>({
    email: "",
    password: "",
  });

  const [registerData, setRegisterData] = useState<RegisterRequest>({
    name: "",
    email: "",
    password: "",
    phone: "",
    role: "mixte", // ✅ Valeur par défaut correspondant au backend
  });

  // Mutations
  const loginMutation = useLogin();
  const registerMutation = useRegister();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      console.log("🚀 Tentative de connexion:", loginData);
      
      await loginMutation.mutateAsync(loginData);
      toast({
        title: "Connexion réussie",
        description: "Vous êtes maintenant connecté.",
      });
      navigate(redirectTo);
    } catch (error: any) {
      console.error("❌ Erreur de connexion:", error);
      toast({
        title: "Erreur de connexion",
        description: error.message || "Vérifiez vos identifiants.",
        variant: "destructive",
      });
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      console.log("🚀 Tentative d'inscription:", registerData);
      
      // ✅ Validation côté client avant envoi
      if (!registerData.name || registerData.name.trim().length === 0) {
        toast({
          title: "Nom requis",
          description: "Veuillez saisir votre nom.",
          variant: "destructive",
        });
        return;
      }
      
      if (!registerData.email || registerData.email.trim().length === 0) {
        toast({
          title: "Email requis",
          description: "Veuillez saisir votre email.",
          variant: "destructive",
        });
        return;
      }
      
      if (!registerData.password || registerData.password.length < 6) {
        toast({
          title: "Mot de passe trop court",
          description: "Le mot de passe doit contenir au moins 6 caractères.",
          variant: "destructive",
        });
        return;
      }

      // ✅ Nettoyage des données avant envoi
      const cleanedData: RegisterRequest = {
        name: registerData.name.trim(),
        email: registerData.email.trim().toLowerCase(),
        password: registerData.password,
        phone: registerData.phone?.trim() || undefined,
        role: registerData.role || "mixte", // ✅ Par défaut "mixte"
      };
      
      console.log("📦 Données nettoyées à envoyer:", cleanedData);
      
      await registerMutation.mutateAsync(cleanedData);
      toast({
        title: "Inscription réussie",
        description: "Votre compte a été créé avec succès.",
      });
      navigate(redirectTo);
    } catch (error: any) {
      console.error("❌ Erreur d'inscription:", error);
      toast({
        title: "Erreur d'inscription",
        description: error.message || "Une erreur est survenue.",
        variant: "destructive",
      });
    }
  };

  const handleOAuth = (provider: "google" | "facebook") => {
    // Redirection vers l'OAuth du backend
    window.location.href = `${import.meta.env.VITE_API_URL || "http://localhost:8081/api"}/auth/oauth2/${provider}`;
  };

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>Connexion / Inscription | Retrouv'Tout</title>
        <meta 
          name="description" 
          content="Connectez-vous par email ou via les réseaux sociaux pour profiter de toutes les fonctionnalités." 
        />
        <link 
          rel="canonical" 
          href={typeof window !== "undefined" ? window.location.href : "/auth"} 
        />
      </Helmet>

      <h1 className="text-3xl font-bold mb-6">Connexion / Inscription</h1>

      <Card className="max-w-xl">
        <CardContent className="pt-6">
          <Tabs defaultValue="login">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="login">Connexion</TabsTrigger>
              <TabsTrigger value="signup">Inscription</TabsTrigger>
            </TabsList>
            
            <TabsContent value="login">
              <form onSubmit={handleLogin} className="grid gap-4">
                <Input 
                  type="email" 
                  placeholder="Email" 
                  required 
                  aria-label="Email"
                  value={loginData.email}
                  onChange={(e) => setLoginData({...loginData, email: e.target.value})}
                />
                <Input 
                  type="password" 
                  placeholder="Mot de passe" 
                  required 
                  aria-label="Mot de passe"
                  value={loginData.password}
                  onChange={(e) => setLoginData({...loginData, password: e.target.value})}
                />
                <Button 
                  type="submit" 
                  variant="hero"
                  disabled={loginMutation.isPending}
                >
                  {loginMutation.isPending ? "Connexion..." : "Se connecter"}
                </Button>
              </form>
            </TabsContent>
            
            <TabsContent value="signup">
              <form onSubmit={handleRegister} className="grid gap-4">
                <Input 
                  placeholder="Nom complet" 
                  required 
                  aria-label="Nom"
                  value={registerData.name}
                  onChange={(e) => setRegisterData({...registerData, name: e.target.value})}
                />
                <Input 
                  type="email" 
                  placeholder="Email" 
                  required 
                  aria-label="Email"
                  value={registerData.email}
                  onChange={(e) => setRegisterData({...registerData, email: e.target.value})}
                />
                <Input 
                  placeholder="Téléphone (optionnel)" 
                  aria-label="Téléphone"
                  value={registerData.phone}
                  onChange={(e) => setRegisterData({...registerData, phone: e.target.value})}
                />
                <Select 
                  value={registerData.role}
                  onValueChange={(value: "retrouveur" | "proprietaire" | "mixte") => 
                    setRegisterData({...registerData, role: value})
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Votre rôle" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="retrouveur">Retrouveur (je trouve des objets)</SelectItem>
                    <SelectItem value="proprietaire">Propriétaire (je cherche mes objets)</SelectItem>
                    <SelectItem value="mixte">Les deux (par défaut)</SelectItem>
                  </SelectContent>
                </Select>
                <Input 
                  type="password" 
                  placeholder="Mot de passe (min. 6 caractères)" 
                  required 
                  aria-label="Mot de passe"
                  minLength={6}
                  value={registerData.password}
                  onChange={(e) => setRegisterData({...registerData, password: e.target.value})}
                />
                <Button 
                  type="submit" 
                  variant="hero"
                  disabled={registerMutation.isPending}
                >
                  {registerMutation.isPending ? "Création..." : "Créer un compte"}
                </Button>
              </form>
            </TabsContent>
          </Tabs>

          <div className="mt-6 grid gap-2">
            <Button 
              variant="outline" 
              onClick={() => handleOAuth("google")}
              type="button"
            >
              Continuer avec Google
            </Button>
            <Button 
              variant="outline" 
              onClick={() => handleOAuth("facebook")}
              type="button"
            >
              Continuer avec Facebook
            </Button>
          </div>
        </CardContent>
      </Card>
    </main>
  );
};

export default Auth;
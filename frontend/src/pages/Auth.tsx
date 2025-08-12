import { Helmet } from "react-helmet-async";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { toast } from "@/hooks/use-toast";

const Auth = () => {
  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    toast({ title: "Authentification démo", description: "Connectez Supabase pour activer la connexion (email & sociaux)." });
  };

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>Connexion / Inscription | Retrouv’Tout</title>
        <meta name="description" content="Connectez-vous par email ou via les réseaux sociaux pour profiter de toutes les fonctionnalités." />
        <link rel="canonical" href={typeof window !== "undefined" ? window.location.href : "/auth"} />
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
              <form onSubmit={onSubmit} className="grid gap-4">
                <Input type="email" placeholder="Email" required aria-label="Email" />
                <Input type="password" placeholder="Mot de passe" required aria-label="Mot de passe" />
                <Button type="submit" variant="hero">Se connecter</Button>
              </form>
            </TabsContent>
            <TabsContent value="signup">
              <form onSubmit={onSubmit} className="grid gap-4">
                <Input placeholder="Nom" required aria-label="Nom" />
                <Input type="email" placeholder="Email" required aria-label="Email" />
                <Input placeholder="Téléphone" aria-label="Téléphone" />
                <Input type="password" placeholder="Mot de passe" required aria-label="Mot de passe" />
                <Button type="submit" variant="hero">Créer un compte</Button>
              </form>
            </TabsContent>
          </Tabs>

          <div className="mt-6 grid gap-2">
            <Button variant="outline">Continuer avec Google</Button>
            <Button variant="outline">Continuer avec Facebook</Button>
          </div>
        </CardContent>
      </Card>
    </main>
  );
};

export default Auth;

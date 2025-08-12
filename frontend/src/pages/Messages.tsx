import { Helmet } from "react-helmet-async";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { toast } from "@/hooks/use-toast";

interface Message { id: string; from: string; text: string; }

const initial: Message[] = [
  { id: "1", from: "Vous", text: "Bonjour, je pense avoir retrouvé votre sac." },
  { id: "2", from: "Alice", text: "Génial ! Où pouvons-nous nous retrouver ?" },
];

const Messages = () => {
  const [messages, setMessages] = useState<Message[]>(initial);
  const [text, setText] = useState("");

  const send = (e: React.FormEvent) => {
    e.preventDefault();
    if (!text.trim()) return;
    setMessages((m) => [...m, { id: Date.now().toString(), from: "Vous", text }]);
    setText("");
    toast({ title: "Messagerie démo", description: "Connectez Supabase pour la messagerie sécurisée en temps réel." });
  };

  return (
    <main className="container mx-auto py-10">
      <Helmet>
        <title>Messages sécurisés | Retrouv’Tout</title>
        <meta name="description" content="Échangez en toute sécurité sans dévoiler vos informations personnelles." />
        <link rel="canonical" href={typeof window !== "undefined" ? window.location.href : "/messages"} />
      </Helmet>

      <h1 className="text-3xl font-bold mb-6">Messages</h1>

      <Card className="grid gap-0 overflow-hidden">
        <CardContent className="pt-6">
          <div className="space-y-3 max-h-[50vh] overflow-auto pr-1">
            {messages.map((m) => (
              <div key={m.id} className="grid">
                <div className="max-w-[80%] rounded-md border bg-card px-3 py-2 text-sm shadow-sm">
                  <p className="font-medium text-sm text-muted-foreground">{m.from}</p>
                  <p>{m.text}</p>
                </div>
              </div>
            ))}
          </div>
          <form onSubmit={send} className="mt-4 flex gap-2">
            <Input value={text} onChange={(e)=>setText(e.target.value)} placeholder="Écrire un message" aria-label="Message" />
            <Button type="submit" variant="hero">Envoyer</Button>
          </form>
        </CardContent>
      </Card>
    </main>
  );
};

export default Messages;

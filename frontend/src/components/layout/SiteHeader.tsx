import { Link, NavLink } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  cn(
    "px-3 py-2 rounded-md text-sm font-medium transition-colors",
    isActive ? "text-primary" : "text-muted-foreground hover:text-foreground"
  );

const SiteHeader = () => {
  return (
    <header className="sticky top-0 z-40 w-full border-b bg-background/80 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto flex h-16 items-center justify-between">
        <Link to="/" className="flex items-center gap-2" aria-label="Retrouv’Tout Accueil">
          <div className="h-8 w-8 rounded-md bg-primary/10 ring-1 ring-primary/20" />
          <span className="text-lg font-semibold tracking-tight">Retrouv’Tout</span>
        </Link>

        <nav className="hidden md:flex items-center gap-1" aria-label="Navigation principale">
          <NavLink to="/" className={navLinkClass} end>
            Accueil
          </NavLink>
          <NavLink to="/annonces" className={navLinkClass}>
            Annonces
          </NavLink>
          <NavLink to="/messages" className={navLinkClass}>
            Messages
          </NavLink>
          <NavLink to="/profil" className={navLinkClass}>
            Profil
          </NavLink>
        </nav>

        <div className="flex items-center gap-2">
          <Button asChild variant="ghost" className="hidden sm:inline-flex">
            <Link to="/auth">Se connecter</Link>
          </Button>
          <Button asChild variant="hero" size="sm">
            <Link to="/poster">Publier</Link>
          </Button>
        </div>
      </div>
    </header>
  );
};

export default SiteHeader;

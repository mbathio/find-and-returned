// src/components/layout/SiteHeader.tsx - VERSION CORRIGÉE COMPLÈTE AVEC MIXTE
import { Link, NavLink } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { 
  DropdownMenu, 
  DropdownMenuContent, 
  DropdownMenuItem, 
  DropdownMenuTrigger,
  DropdownMenuSeparator 
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { useAuth } from "@/contexts/AuthContext";
import { useUnreadCount } from "@/services/messages";
import { User, LogOut, MessageSquare, Plus, Search } from "lucide-react";

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  cn(
    "px-3 py-2 rounded-md text-sm font-medium transition-colors",
    isActive ? "text-primary" : "text-muted-foreground hover:text-foreground"
  );

const SiteHeader = () => {
  const { user, isAuthenticated, logout } = useAuth();
  
  // ✅ CORRECTION : Ne charger le compteur que si l'utilisateur est authentifié
  const { data: unreadCount } = useUnreadCount();

  const handleLogout = async () => {
    await logout();
  };

  const getUserInitials = (name: string) => {
    return name
      .split(" ")
      .map(word => word.charAt(0))
      .join("")
      .toUpperCase()
      .slice(0, 2);
  };

  const getRoleLabel = (role: string) => {
    switch (role) {
      case "retrouveur":
        return "Retrouveur";
      case "proprietaire":
        return "Propriétaire";
      case "mixte":
        return "Mixte";
      default:
        return "Utilisateur";
    }
  };

  return (
    <header className="sticky top-0 z-40 w-full border-b bg-background/80 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto flex h-16 items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2" aria-label="Retrouv'Tout Accueil">
          <div className="h-8 w-8 rounded-md bg-primary/10 ring-1 ring-primary/20 flex items-center justify-center">
            <span className="text-primary font-bold text-sm">RT</span>
          </div>
          <span className="text-lg font-semibold tracking-tight">Retrouv'Tout</span>
        </Link>

        {/* Navigation principale */}
        <nav className="hidden md:flex items-center gap-1" aria-label="Navigation principale">
          <NavLink to="/" className={navLinkClass} end>
            Accueil
          </NavLink>
          <NavLink to="/annonces" className={navLinkClass}>
            Annonces
          </NavLink>
          {isAuthenticated && (
            <NavLink to="/messages" className={navLinkClass}>
              <div className="flex items-center gap-1">
                Messages
                {unreadCount && unreadCount > 0 && (
                  <Badge variant="destructive" className="h-5 w-5 p-0 text-xs flex items-center justify-center">
                    {unreadCount > 99 ? "99+" : unreadCount}
                  </Badge>
                )}
              </div>
            </NavLink>
          )}
        </nav>

        {/* Actions utilisateur */}
        <div className="flex items-center gap-2">
          {isAuthenticated ? (
            <>
              {/* Bouton publier */}
              <Button asChild variant="hero" size="sm" className="hidden sm:inline-flex">
                <Link to="/poster">
                  <Plus className="mr-2 h-4 w-4" />
                  Publier
                </Link>
              </Button>

              {/* Menu utilisateur */}
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="sm" className="relative h-9 w-9 rounded-full">
                    <Avatar className="h-8 w-8">
                      <AvatarFallback className="text-xs">
                        {user ? getUserInitials(user.name) : "U"}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <div className="flex items-center justify-start gap-2 p-2">
                    <div className="flex flex-col space-y-1 leading-none">
                      <p className="font-medium">{user?.name}</p>
                      <p className="w-[200px] truncate text-sm text-muted-foreground">
                        {user?.email}
                      </p>
                      <Badge variant="secondary" className="w-fit text-xs">
                        {user?.role ? getRoleLabel(user.role) : "Utilisateur"}
                      </Badge>
                    </div>
                  </div>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem asChild>
                    <Link to="/profil" className="cursor-pointer">
                      <User className="mr-2 h-4 w-4" />
                      Mon profil
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link to="/messages" className="cursor-pointer">
                      <MessageSquare className="mr-2 h-4 w-4" />
                      Messages
                      {unreadCount && unreadCount > 0 && (
                        <Badge variant="destructive" className="ml-auto h-5 w-5 p-0 text-xs">
                          {unreadCount > 99 ? "99+" : unreadCount}
                        </Badge>
                      )}
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild className="md:hidden">
                    <Link to="/poster" className="cursor-pointer">
                      <Plus className="mr-2 h-4 w-4" />
                      Publier une annonce
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout} className="cursor-pointer text-destructive">
                    <LogOut className="mr-2 h-4 w-4" />
                    Se déconnecter
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <>
              {/* Boutons non authentifié */}
              <Button asChild variant="ghost" className="hidden sm:inline-flex">
                <Link to="/auth">Se connecter</Link>
              </Button>
              <Button asChild variant="hero" size="sm">
                <Link to="/poster">Publier</Link>
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Navigation mobile */}
      {isAuthenticated && (
        <div className="md:hidden border-t bg-background">
          <nav className="container mx-auto flex items-center justify-around py-2">
            <NavLink 
              to="/annonces" 
              className="flex flex-col items-center gap-1 p-2 text-xs"
            >
              <Search className="h-4 w-4" />
              Annonces
            </NavLink>
            <NavLink 
              to="/messages" 
              className="flex flex-col items-center gap-1 p-2 text-xs relative"
            >
              <MessageSquare className="h-4 w-4" />
              Messages
              {unreadCount && unreadCount > 0 && (
                <Badge variant="destructive" className="absolute -top-1 -right-1 h-4 w-4 p-0 text-xs">
                  {unreadCount > 9 ? "9+" : unreadCount}
                </Badge>
              )}
            </NavLink>
            <NavLink 
              to="/poster" 
              className="flex flex-col items-center gap-1 p-2 text-xs"
            >
              <Plus className="h-4 w-4" />
              Publier
            </NavLink>
            <NavLink 
              to="/profil" 
              className="flex flex-col items-center gap-1 p-2 text-xs"
            >
              <User className="h-4 w-4" />
              Profil
            </NavLink>
          </nav>
        </div>
      )}
    </header>
  );
};

export default SiteHeader;
// src/components/layout/SiteHeader.tsx - CORRECTION AVEC STARTTRANSITION
import { useState, useTransition, startTransition } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useAuth } from "@/contexts/AuthContext";
import { useUnreadCount } from "@/services/messages";
import { useLogout } from "@/services/auth";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { User, Bell, LogOut, Menu } from "lucide-react";

const SiteHeader = () => {
  const { user, isAuthenticated } = useAuth();
  const [isPending, startTransition] = useTransition();
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  
  // ✅ CORRECTION : useUnreadCount ne se déclenche que si authentifié
  const { data: unreadCount = 0, error: unreadError } = useUnreadCount();
  
  const logoutMutation = useLogout();

  const handleLogout = () => {
    // ✅ CORRECTION : Entourer dans startTransition
    startTransition(() => {
      logoutMutation.mutate();
    });
  };

  const handleMobileMenuToggle = () => {
    // ✅ CORRECTION : Entourer les updates d'état dans startTransition
    startTransition(() => {
      setShowMobileMenu(!showMobileMenu);
    });
  };

  // ✅ Gestion d'erreur pour le compteur non lu
  const displayUnreadCount = unreadError ? 0 : unreadCount;

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur">
      <div className="container mx-auto flex h-14 items-center justify-between px-4">
        
        {/* Logo */}
        <Link to="/" className="flex items-center space-x-2">
          <div className="h-8 w-8 rounded-full bg-gradient-to-br from-primary to-primary-foreground" />
          <span className="font-bold">Retrouv'Tout</span>
        </Link>

        {/* Navigation desktop */}
        <nav className="hidden md:flex items-center space-x-6">
          <Link to="/annonces" className="text-sm font-medium hover:text-primary transition-colors">
            Annonces
          </Link>
          <Link to="/poster" className="text-sm font-medium hover:text-primary transition-colors">
            Publier
          </Link>
          
          {isAuthenticated && (
            <Link to="/messages" className="flex items-center space-x-1 text-sm font-medium hover:text-primary transition-colors">
              <Bell size={16} />
              <span>Messages</span>
              {displayUnreadCount > 0 && (
                <Badge variant="destructive" className="text-xs">
                  {displayUnreadCount > 99 ? "99+" : displayUnreadCount}
                </Badge>
              )}
            </Link>
          )}
        </nav>

        {/* Actions utilisateur */}
        <div className="flex items-center space-x-2">
          {isAuthenticated ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="flex items-center space-x-2">
                  <User size={16} />
                  <span className="hidden sm:inline-block max-w-24 truncate">
                    {user?.name || "Utilisateur"}
                  </span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel>Mon compte</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild>
                  <Link to="/profil" className="cursor-pointer">
                    Profil
                  </Link>
                </DropdownMenuItem>
                <DropdownMenuItem asChild>
                  <Link to="/messages" className="cursor-pointer">
                    Messages
                    {displayUnreadCount > 0 && (
                      <Badge variant="secondary" className="ml-auto text-xs">
                        {displayUnreadCount}
                      </Badge>
                    )}
                  </Link>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem 
                  onClick={handleLogout}
                  className="cursor-pointer text-destructive focus:text-destructive"
                  disabled={isPending}
                >
                  <LogOut className="mr-2 h-4 w-4" />
                  {isPending ? "Déconnexion..." : "Se déconnecter"}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <Button asChild variant="hero" size="sm">
              <Link to="/auth">Connexion</Link>
            </Button>
          )}

          {/* Bouton menu mobile */}
          <Button
            variant="ghost"
            size="sm"
            className="md:hidden"
            onClick={handleMobileMenuToggle}
            disabled={isPending}
          >
            <Menu size={16} />
          </Button>
        </div>
      </div>

      {/* Menu mobile */}
      {showMobileMenu && (
        <div className="md:hidden border-t bg-background/95 backdrop-blur">
          <nav className="container mx-auto py-4 flex flex-col space-y-2">
            <Link 
              to="/annonces" 
              className="px-4 py-2 text-sm font-medium hover:bg-accent rounded-md transition-colors"
              onClick={() => startTransition(() => setShowMobileMenu(false))}
            >
              Annonces
            </Link>
            <Link 
              to="/poster" 
              className="px-4 py-2 text-sm font-medium hover:bg-accent rounded-md transition-colors"
              onClick={() => startTransition(() => setShowMobileMenu(false))}
            >
              Publier
            </Link>
            {isAuthenticated && (
              <Link 
                to="/messages" 
                className="px-4 py-2 text-sm font-medium hover:bg-accent rounded-md transition-colors flex items-center justify-between"
                onClick={() => startTransition(() => setShowMobileMenu(false))}
              >
                <span>Messages</span>
                {displayUnreadCount > 0 && (
                  <Badge variant="destructive" className="text-xs">
                    {displayUnreadCount > 99 ? "99+" : displayUnreadCount}
                  </Badge>
                )}
              </Link>
            )}
          </nav>
        </div>
      )}
    </header>
  );
};

export default SiteHeader;
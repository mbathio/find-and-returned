import { Suspense, lazy } from "react";
import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { HelmetProvider } from "react-helmet-async";
import { ErrorBoundary } from "@/components/ErrorBoundary";
import { PageSkeleton } from "@/components/ui/PageSkeleton";
import SiteHeader from "./components/layout/SiteHeader";
import SiteFooter from "./components/layout/SiteFooter";
import { NotificationProvider } from "./components/providers/NotificationProvider";

// Lazy loading des composants de pages
const Index = lazy(() => import("./pages/Index"));
const Annonces = lazy(() => import("./pages/Annonces"));
const AnnonceDetail = lazy(() => import("./pages/AnnonceDetail"));
const Poster = lazy(() => import("./pages/Poster"));
const Auth = lazy(() => import("./pages/Auth"));
const Profil = lazy(() => import("./pages/Profil"));
const Messages = lazy(() => import("./pages/Messages"));
const NotFound = lazy(() => import("./pages/NotFound"));

// Configuration React Query optimisée
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes (anciennement cacheTime)
      retry: (failureCount, error: unknown) => {
        // Ne pas retry sur les erreurs 4xx
        if (error && typeof error === "object" && "status" in error) {
          const status = error.status as number;
          if (status >= 400 && status < 500) {
            return false;
          }
        }
        return failureCount < 3;
      },
      refetchOnWindowFocus: false,
      refetchOnReconnect: true,
    },
    mutations: {
      retry: 1,
    },
  },
});

const App = () => {
  return (
    <ErrorBoundary>
      <HelmetProvider>
        <QueryClientProvider client={queryClient}>
          <TooltipProvider>
            <NotificationProvider>
              <Toaster />
              <Sonner />
              <BrowserRouter>
                <SiteHeader />
                <main className="min-h-screen">
                  <Suspense fallback={<PageSkeleton />}>
                    <Routes>
                      <Route path="/" element={<Index />} />
                      <Route path="/annonces" element={<Annonces />} />
                      <Route path="/annonces/:id" element={<AnnonceDetail />} />
                      <Route path="/poster" element={<Poster />} />
                      <Route path="/auth" element={<Auth />} />
                      <Route path="/profil" element={<Profil />} />
                      <Route path="/messages" element={<Messages />} />
                      <Route path="*" element={<NotFound />} />
                    </Routes>
                  </Suspense>
                </main>
                <SiteFooter />
              </BrowserRouter>
            </NotificationProvider>
          </TooltipProvider>
          {process.env.NODE_ENV === "development" && <ReactQueryDevtools />}
        </QueryClientProvider>
      </HelmetProvider>
    </ErrorBoundary>
  );
};

export default App;

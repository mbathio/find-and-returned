// src/components/ErrorBoundary.tsx
import React, { Component, ReactNode, ErrorInfo } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Error caught by boundary:", error, errorInfo);
    // Ici vous pouvez envoyer l'erreur à un service de monitoring
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center p-4">
          <Card className="max-w-md w-full">
            <CardHeader>
              <CardTitle className="text-destructive">
                Oups ! Une erreur est survenue
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <p className="text-sm text-muted-foreground">
                Nous rencontrons des difficultés techniques. Veuillez recharger
                la page.
              </p>
              <div className="flex gap-2">
                <Button
                  onClick={() => window.location.reload()}
                  variant="default"
                >
                  Recharger la page
                </Button>
                <Button onClick={() => window.history.back()} variant="outline">
                  Retour
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      );
    }

    return this.props.children;
  }
}

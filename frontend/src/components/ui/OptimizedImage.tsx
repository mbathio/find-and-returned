// src/components/ui/OptimizedImage.tsx
import { useState, useRef, useEffect } from "react";
import { cn } from "@/lib/utils";

interface OptimizedImageProps
  extends React.ImgHTMLAttributes<HTMLImageElement> {
  src: string;
  alt: string;
  fallback?: string;
  className?: string;
}

export const OptimizedImage = ({
  src,
  alt,
  fallback = "/placeholder.svg",
  className,
  ...props
}: OptimizedImageProps) => {
  const [imageSrc, setImageSrc] = useState<string>(fallback);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const imgRef = useRef<HTMLImageElement>(null);

  useEffect(() => {
    const img = new Image();
    img.onload = () => {
      setImageSrc(src);
      setIsLoading(false);
    };
    img.onerror = () => {
      setHasError(true);
      setIsLoading(false);
    };
    img.src = src;
  }, [src]);

  return (
    <div className={cn("relative overflow-hidden", className)}>
      {isLoading && <div className="absolute inset-0 bg-muted animate-pulse" />}
      <img
        ref={imgRef}
        src={imageSrc}
        alt={alt}
        loading="lazy"
        decoding="async"
        style={{
          contentVisibility: "auto",
          transition: "opacity 0.3s ease-in-out",
        }}
        className={cn(
          "w-full h-full object-cover",
          isLoading && "opacity-0",
          hasError && "opacity-50"
        )}
        {...props}
      />
      {hasError && (
        <div className="absolute inset-0 flex items-center justify-center bg-muted">
          <span className="text-muted-foreground text-sm">
            Image non disponible
          </span>
        </div>
      )}
    </div>
  );
};

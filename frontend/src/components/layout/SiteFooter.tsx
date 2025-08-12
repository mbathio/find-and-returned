const SiteFooter = () => {
  return (
    <footer className="border-t bg-background">
      <div className="container mx-auto py-8 text-center text-sm text-muted-foreground">
        <p>
          © {new Date().getFullYear()} Retrouv’Tout • Retrouvez et signalez des objets perdus.
        </p>
      </div>
    </footer>
  );
};

export default SiteFooter;

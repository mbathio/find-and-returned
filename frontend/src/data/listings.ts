// Types conformes au cahier des charges avec fonctionnalités étendues conservées
export interface Listing {
  id: string;
  title: string; // type d'objet
  category: string; // catégories étendues conservées
  location: string; // lieu de découverte
  date: string; // date de découverte (ISO string)
  description: string; // description
  image?: string; // photo
  finderUserId?: string; // ID du retrouveur
  status?: "active" | "resolu";
}

// Données d'exemple avec toutes les catégories originales conservées
export const listings: Listing[] = [
  {
    id: "1",
    title: "Trousseau de clés Opel",
    category: "Clés",
    location: "Gare de Lyon",
    date: "2025-08-05",
    description: "Trouvé près de la sortie 3, porte-clés jaune.",
    image: "/Trousseau-de-clés.jpg",
  },
  {
    id: "2",
    title: "Sac à dos noir",
    category: "Bagagerie",
    location: "Université Paris Cité",
    date: "2025-08-06",
    description: "Contient des cahiers, sans papiers d'identité visibles.",
    image: "/Sac-à-dos-noir.jpg",
  },
  {
    id: "3",
    title: "iPhone 13 bleu",
    category: "Électronique",
    location: "Tram T3a - Porte de Vincennes",
    date: "2025-08-07",
    description: "Code verrouillé, coque transparente.",
    image: "/iPhone-13-bleu.jpg",
  },
];

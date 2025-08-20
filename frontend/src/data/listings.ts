export interface Listing {
  id: string;
  title: string;
  category: string;
  location: string;
  date: string; // ISO string
  description: string;
  image?: string;
}

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
    description: "Contient des cahiers, sans papiers d’identité visibles.",
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

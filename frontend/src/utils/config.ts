// src/utils/config.ts - Utilitaire pour vérifier la configuration
export const config = {
  apiUrl: import.meta.env.VITE_API_URL || "http://localhost:8081/api",
  appName: import.meta.env.VITE_APP_NAME || "Retrouv'Tout",
  isDev: import.meta.env.DEV,
  isProd: import.meta.env.PROD,
};

// Vérification de la configuration au démarrage
export const verifyConfig = () => {
  console.group("🔧 Configuration de l'application");
  console.log("API URL:", config.apiUrl);
  console.log("App Name:", config.appName);
  console.log("Environment:", config.isDev ? "Development" : "Production");
  console.log("Variables d'environnement disponibles:");
  
  Object.keys(import.meta.env).forEach(key => {
    if (key.startsWith('VITE_')) {
      console.log(`  ${key}:`, import.meta.env[key]);
    }
  });
  
  console.groupEnd();
  
  // Test de connectivité API
  testApiConnection();
};

const testApiConnection = async () => {
  try {
    const response = await fetch(`${config.apiUrl}/health`);
    if (response.ok) {
      console.log("✅ Connexion API réussie");
    } else {
      console.warn("⚠️ API répond mais avec erreur:", response.status);
    }
  } catch (error) {
    console.error("❌ Impossible de contacter l'API:", error);
    console.error("🔍 Vérifiez que le backend fonctionne sur:", config.apiUrl);
  }
};

// Appeler au démarrage de l'app
if (config.isDev) {
  verifyConfig();
}
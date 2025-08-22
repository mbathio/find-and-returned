// src/main.tsx - VERSION CORRIGÉE
import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import './index.css'

// ✅ Vérification de la configuration au démarrage
import './utils/config.ts'

// ✅ Un seul appel de rendu
createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
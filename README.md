# 🔍 Retrouv'Tout - Plateforme Objets Perdus et Retrouvés

Une plateforme web moderne pour faciliter la mise en relation entre les personnes qui trouvent des objets perdus et leurs propriétaires légitimes.

## 🌟 Fonctionnalités

### Pour les Trouveurs

- 📸 **Publication facile** - Ajoutez une photo, lieu, date de découverte
- 🗺️ **Géolocalisation** - Localisez précisément où l'objet a été trouvé
- 💬 **Messagerie sécurisée** - Échangez sans révéler vos coordonnées
- 🔔 **Notifications** - Soyez alerté quand quelqu'un recherche l'objet

### Pour les Propriétaires

- 🔍 **Recherche avancée** - Filtrez par lieu, date, catégorie, description
- 📍 **Recherche géographique** - Trouvez dans votre zone
- 🚨 **Alertes personnalisées** - Recevez des notifications pour vos objets
- ✅ **Système de vérification** - Prouvez votre propriété en sécurité

### Sécurité & Confidentialité

- 🔐 **Authentification sécurisée** - Connexion par email ou réseaux sociaux
- 🛡️ **Protection des données** - Vos informations personnelles restent privées
- 📱 **Code de remise** - Système sécurisé pour récupérer vos objets
- ⚡ **Signalement** - Signaler les comportements suspects

## 🏗️ Architecture Technique

### Frontend (`/frontend`)

- **React 18** + **TypeScript** - Interface utilisateur moderne
- **Vite** - Build tool rapide et optimisé
- **Tailwind CSS** + **shadcn/ui** - Design system cohérent
- **React Query** - Gestion d'état et cache intelligent
- **React Router** - Navigation fluide
- **PWA** - Installable comme une app native

### Backend (`/backend`)

- **Node.js** + **TypeScript** - API REST performante
- **Express.js** - Framework web robuste
- **MySQL** - Base de données relationnelle
- **JWT** - Authentification sécurisée
- **Knex.js** - Migrations et requêtes optimisées
- **Multer** - Upload de fichiers sécurisé

## 🚀 Installation et Développement

### Prérequis

- **Node.js** 18+ et **npm**
- **MySQL** 8.0+
- **Git**

### Installation Rapide

```bash
# 1. Cloner le projet
git clone https://github.com/mbathio/find-and-returned.git
cd find-and-returned

# 2. Installer et démarrer le backend
cd backend
npm install
cp .env.example .env
# Configurez votre .env (voir backend/README.md)
npm run db:migrate
npm run dev

# 3. Dans un autre terminal, installer et démarrer le frontend
cd ../frontend
npm install
npm run dev
```

### URLs de développement

- 🌐 **Frontend** : http://localhost:3000
- 🔌 **Backend API** : http://localhost:8081
- 📚 **Documentation API** : http://localhost:8081/api-docs

## 📱 Progressive Web App

L'application peut être installée sur mobile et desktop avec :

- ✅ **Mode hors ligne** - Consultez vos annonces sans internet
- 🔔 **Notifications push** - Alertes en temps réel
- 📱 **Installation native** - Ajoutez à l'écran d'accueil
- ⚡ **Performances optimales** - Chargement ultra-rapide

## 📊 Base de Données

```
📁 Schéma MySQL Principal
├── users           # Utilisateurs et authentification
├── listings        # Annonces d'objets trouvés
├── threads         # Conversations entre utilisateurs
├── messages        # Messages privés
├── alerts          # Alertes de recherche personnalisées
├── confirmations   # Codes de remise sécurisés
└── reports         # Signalements et modération
```

## 🛠️ Scripts de Développement

### Frontend

```bash
npm run dev         # Serveur de développement
npm run build       # Build de production
npm run preview     # Aperçu du build
npm test            # Tests unitaires
```

### Backend

```bash
npm run dev         # Serveur avec hot reload
npm run build       # Compilation TypeScript
npm start           # Production
npm run db:migrate  # Migrations base de données
npm test            # Tests API
```

## 🔧 Configuration

### Variables d'environnement

**Backend** (`.env`) :

```bash
# Base de données
DB_HOST=localhost
DB_NAME=retrouvtout
DB_USER=your_user
DB_PASSWORD=your_password

# Sécurité
JWT_SECRET=your-super-secret-key

# Email
SMTP_HOST=smtp.gmail.com
SMTP_USER=your-email@gmail.com
```

**Frontend** (`.env.local`) :

```bash
VITE_API_URL=http://localhost:8081/api
VITE_APP_NAME="Retrouv'Tout"
```

## 📚 Documentation

- 📖 **Guide utilisateur** : [`/docs/user-guide.md`](./docs/user-guide.md)
- 🔌 **API REST** : [`/backend/docs/api.md`](./backend/docs/api.md)
- 🎨 **Guide design** : [`/frontend/docs/design-system.md`](./frontend/docs/design-system.md)
- 🗄️ **Schéma DB** : [`/backend/docs/database-schema.md`](./backend/docs/database-schema.md)

## 🚦 Statut du Projet

- ✅ **Interface utilisateur** - Design et navigation complets
- ✅ **Base de données** - Schéma et relations optimisés
- 🔄 **API Backend** - Endpoints en développement
- 🔄 **Tests** - Couverture en cours d'amélioration
- 📋 **Déploiement** - Scripts de production en préparation

## 🤝 Contribution

1. **Fork** le projet
2. **Créez** une branche feature (`git checkout -b feature/nouvelle-fonctionnalite`)
3. **Commitez** vos changements (`git commit -m 'Ajout: nouvelle fonctionnalité'`)
4. **Push** vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. **Ouvrez** une Pull Request

### Standards de code

- **TypeScript** strict activé
- **ESLint** + **Prettier** pour le formatage
- **Tests unitaires** requis pour les nouvelles fonctionnalités
- **Documentation** mise à jour

## 📄 Licence

Ce projet est sous licence **MIT**. Voir le fichier [LICENSE](./LICENSE) pour plus de détails.

## 👥 Équipe

- **Lead Developer** : [Mbathio](https://github.com/mbathio)
- **Contributors** : Voir [CONTRIBUTORS.md](./CONTRIBUTORS.md)

## 📞 Support

- 🐛 **Bugs** : [GitHub Issues](https://github.com/mbathio/find-and-returned/issues)
- 💡 **Suggestions** : [GitHub Discussions](https://github.com/mbathio/find-and-returned/discussions)
- 📧 **Contact** : mbathio@example.com

---

<div align="center">

**⭐ Si ce projet vous aide, n'hésitez pas à lui donner une étoile ! ⭐**

[🌐 Demo Live](https://retrouvtout.com) • [📱 Télécharger l'app](https://app.retrouvtout.com) • [📖 Documentation](https://docs.retrouvtout.com)

</div>

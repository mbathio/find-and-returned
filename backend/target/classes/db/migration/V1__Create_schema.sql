-- Schema strictement conforme au cahier des charges
-- UNIQUEMENT les fonctionnalités spécifiées

-- Table des utilisateurs - Section 3.1 du cahier des charges
-- Gestion des informations personnelles (nom, email, téléphone)
-- Rôles STRICTEMENT : retrouveur vs proprietaire
CREATE TABLE users (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NULL,
    phone VARCHAR(40) NULL, -- Pour notifications SMS (Section 3.3)
    role ENUM('retrouveur', 'proprietaire') NOT NULL DEFAULT 'retrouveur',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table OAuth pour connexion réseaux sociaux - Section 3.1
-- Inscription/Connexion via email ou réseaux sociaux
CREATE TABLE oauth_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    provider VARCHAR(40) NOT NULL, -- google, facebook
    provider_user_id VARCHAR(190) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    
    UNIQUE KEY ux_oauth_provider_user (provider, provider_user_id),
    INDEX idx_oauth_user (user_id),
    
    CONSTRAINT fk_oauth_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des annonces - Section 3.2 du cahier des charges
-- Champs requis : type d'objet, lieu de découverte, date, photo, description, catégorie
-- Catégories EXACTEMENT conformes au frontend : cles, electronique, bagagerie, documents, vetements, autre
CREATE TABLE listings (
    id CHAR(36) NOT NULL PRIMARY KEY,
    finder_user_id CHAR(36) NOT NULL,
    title VARCHAR(180) NOT NULL, -- Type d'objet
    category ENUM('cles', 'electronique', 'bagagerie', 'documents', 'vetements', 'autre') NOT NULL,
    location_text VARCHAR(255) NOT NULL, -- Lieu de découverte
    latitude DECIMAL(9,6) NULL, -- Pour filtres géographiques
    longitude DECIMAL(9,6) NULL,
    found_at DATETIME(3) NOT NULL, -- Date de découverte
    description TEXT NOT NULL,
    image_url VARCHAR(512) NULL, -- Photo
    status ENUM('active', 'resolu', 'suspendu', 'supprime') NOT NULL DEFAULT 'active',
    views_count BIGINT NOT NULL DEFAULT 0,
    is_moderated BOOLEAN NOT NULL DEFAULT TRUE, -- Modération simple - Section 3.4
    moderated_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    
    INDEX idx_listings_finder (finder_user_id),
    INDEX idx_listings_category (category),
    INDEX idx_listings_status (status),
    INDEX idx_listings_found_at (found_at),
    INDEX idx_listings_location (latitude, longitude),
    INDEX idx_listings_moderated (is_moderated),
    
    -- Index pour recherche textuelle - Section 3.2 (moteur de recherche)
    FULLTEXT INDEX ftx_listings_text (title, description),
    
    CONSTRAINT fk_listings_finder 
        FOREIGN KEY (finder_user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table pour images multiples d'une annonce
CREATE TABLE listing_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id CHAR(36) NOT NULL,
    url VARCHAR(512) NOT NULL,
    alt_text VARCHAR(255) NULL,
    file_size BIGINT NULL,
    mime_type VARCHAR(50) NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    
    INDEX idx_images_listing (listing_id),
    INDEX idx_images_primary (is_primary),
    
    CONSTRAINT fk_images_listing 
        FOREIGN KEY (listing_id) REFERENCES listings(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des conversations - Section 3.5 du cahier des charges
-- Messagerie intégrée pour communication directe via la plateforme
CREATE TABLE threads (
    id CHAR(36) NOT NULL PRIMARY KEY,
    listing_id CHAR(36) NOT NULL,
    owner_user_id CHAR(36) NOT NULL, -- Propriétaire (qui a perdu l'objet)
    finder_user_id CHAR(36) NOT NULL, -- Retrouveur (qui a trouvé l'objet)
    status ENUM('active', 'closed') NOT NULL DEFAULT 'active',
    last_message_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    
    INDEX idx_threads_listing (listing_id),
    INDEX idx_threads_owner (owner_user_id),
    INDEX idx_threads_finder (finder_user_id),
    INDEX idx_threads_status (status),
    INDEX idx_threads_last_message (last_message_at),
    
    UNIQUE KEY ux_threads_listing_owner (listing_id, owner_user_id),
    
    CONSTRAINT fk_threads_listing 
        FOREIGN KEY (listing_id) REFERENCES listings(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_threads_owner 
        FOREIGN KEY (owner_user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_threads_finder 
        FOREIGN KEY (finder_user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des messages - Section 3.5 du cahier des charges
-- Communication directe via la plateforme
-- Masquage des informations personnelles - Section 3.4
CREATE TABLE messages (
    id CHAR(36) NOT NULL PRIMARY KEY,
    thread_id CHAR(36) NOT NULL,
    sender_user_id CHAR(36) NOT NULL,
    body TEXT NOT NULL,
    message_type ENUM('text', 'system') NOT NULL DEFAULT 'text',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    
    INDEX idx_messages_thread (thread_id),
    INDEX idx_messages_sender (sender_user_id),
    INDEX idx_messages_created_at (created_at),
    INDEX idx_messages_read (is_read),
    
    CONSTRAINT fk_messages_thread 
        FOREIGN KEY (thread_id) REFERENCES threads(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender 
        FOREIGN KEY (sender_user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table pour logs de notifications - Section 3.3 du cahier des charges
-- Alertes email/SMS et notifications push
CREATE TABLE notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    type ENUM('email', 'sms', 'push') NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status ENUM('pending', 'sent', 'delivered', 'failed') NOT NULL DEFAULT 'pending',
    listing_id CHAR(36) NULL, -- Lié à une annonce si applicable
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    sent_at DATETIME(3) NULL,
    delivered_at DATETIME(3) NULL,
    
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_listing (listing_id),
    INDEX idx_notifications_created_at (created_at),
    
    CONSTRAINT fk_notifications_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_notifications_listing 
        FOREIGN KEY (listing_id) REFERENCES listings(id) 
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
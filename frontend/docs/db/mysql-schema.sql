-- Retrouv’Tout MySQL schema (MySQL 8.0+)
-- Charset & engine
SET NAMES utf8mb4;
SET time_zone = '+00:00';

CREATE TABLE users (
  id            CHAR(36)     NOT NULL,
  name          VARCHAR(120) NOT NULL,
  email         VARCHAR(190) NOT NULL,
  password_hash VARCHAR(255) NULL,
  phone         VARCHAR(40)  NULL,
  role          ENUM('retrouveur','proprietaire','mixte') NOT NULL DEFAULT 'mixte',
  created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY ux_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oauth_accounts (
  id               BIGINT       NOT NULL AUTO_INCREMENT,
  user_id          CHAR(36)     NOT NULL,
  provider         VARCHAR(40)  NOT NULL,
  provider_user_id VARCHAR(190) NOT NULL,
  created_at       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY ux_oauth_provider_user (provider, provider_user_id),
  KEY ix_oauth_user (user_id),
  CONSTRAINT fk_oauth_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE listings (
  id             CHAR(36)     NOT NULL,
  finder_user_id CHAR(36)     NOT NULL,
  title          VARCHAR(180) NOT NULL,
  category       ENUM('cles','electronique','bagagerie','documents','autre') NOT NULL,
  location_text  VARCHAR(255) NOT NULL,
  latitude       DECIMAL(9,6) NULL,
  longitude      DECIMAL(9,6) NULL,
  found_at       DATETIME(3)  NOT NULL,
  description    TEXT         NOT NULL,
  image_url      VARCHAR(512) NULL,
  status         ENUM('active','resolu') NOT NULL DEFAULT 'active',
  created_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_listings_finder (finder_user_id),
  KEY ix_listings_category (category),
  KEY ix_listings_found_at (found_at),
  SPATIAL INDEX ix_listings_geo ((POINT(longitude, latitude))),
  FULLTEXT KEY ftx_listings_text (title, description),
  CONSTRAINT fk_listings_finder FOREIGN KEY (finder_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE listing_images (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  listing_id CHAR(36)     NOT NULL,
  url        VARCHAR(512) NOT NULL,
  created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_images_listing (listing_id),
  CONSTRAINT fk_images_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE threads (
  id               CHAR(36)    NOT NULL,
  listing_id       CHAR(36)    NOT NULL,
  owner_user_id    CHAR(36)    NOT NULL,
  finder_user_id   CHAR(36)    NOT NULL,
  status           ENUM('pending','approved','closed') NOT NULL DEFAULT 'pending',
  approved_owner   TINYINT(1)  NOT NULL DEFAULT 0,
  approved_finder  TINYINT(1)  NOT NULL DEFAULT 0,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_threads_listing (listing_id),
  KEY ix_threads_owner (owner_user_id),
  KEY ix_threads_finder (finder_user_id),
  CONSTRAINT fk_threads_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
  CONSTRAINT fk_threads_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_threads_finder FOREIGN KEY (finder_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE messages (
  id            CHAR(36)     NOT NULL,
  thread_id     CHAR(36)     NOT NULL,
  sender_user_id CHAR(36)    NOT NULL,
  body          TEXT         NOT NULL,
  created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_messages_thread (thread_id),
  KEY ix_messages_sender (sender_user_id),
  CONSTRAINT fk_messages_thread FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
  CONSTRAINT fk_messages_sender FOREIGN KEY (sender_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE confirmations (
  id         CHAR(36)    NOT NULL,
  thread_id  CHAR(36)    NOT NULL,
  code       VARCHAR(12) NOT NULL,
  expires_at DATETIME(3) NOT NULL,
  used_at    DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY ux_confirmations_thread (thread_id),
  CONSTRAINT fk_confirmations_thread FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE alerts (
  id            CHAR(36)    NOT NULL,
  owner_user_id CHAR(36)    NOT NULL,
  q             VARCHAR(255) NULL,
  category      VARCHAR(40)  NULL,
  location_text VARCHAR(255) NULL,
  latitude      DECIMAL(9,6) NULL,
  longitude     DECIMAL(9,6) NULL,
  radius_km     DECIMAL(6,2) NULL,
  date_from     DATE         NULL,
  date_to       DATE         NULL,
  channels      JSON         NOT NULL,
  active        TINYINT(1)   NOT NULL DEFAULT 1,
  created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY ix_alerts_owner (owner_user_id),
  CONSTRAINT fk_alerts_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE moderation_flags (
  id               BIGINT       NOT NULL AUTO_INCREMENT,
  entity_type      ENUM('listing','message','user') NOT NULL,
  entity_id        VARCHAR(64)  NOT NULL,
  reason           VARCHAR(255) NOT NULL,
  status           ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending',
  created_by_user_id CHAR(36)   NULL,
  reviewed_by_user_id CHAR(36)  NULL,
  created_at       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  reviewed_at      DATETIME(3)  NULL,
  PRIMARY KEY (id),
  KEY ix_flags_entity (entity_type, entity_id),
  KEY ix_flags_status (status),
  CONSTRAINT fk_flags_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT fk_flags_reviewer FOREIGN KEY (reviewed_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE push_subscriptions (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  user_id    CHAR(36)     NOT NULL,
  endpoint   VARCHAR(512) NOT NULL,
  keys       JSON         NOT NULL,
  created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY ux_push_endpoint (endpoint),
  KEY ix_push_user (user_id),
  CONSTRAINT fk_push_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Performance suggestions
-- 1) Utiliser FULLTEXT sur (title, description) pour la recherche par mots-clés
-- 2) Indexer (category, found_at) pour filtres
-- 3) Géolocalisation: stocker aussi POINT(longitude, latitude) si besoin d’index R-tree; MySQL 8 permet SRS
-- 4) Paginer via (created_at, id) pour éviter OFFSET lourds sur grandes tables

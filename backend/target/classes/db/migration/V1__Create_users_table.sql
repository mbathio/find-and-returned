-- V1__Create_users_table.sql
CREATE TABLE users (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NULL,
    phone VARCHAR(40) NULL,
    role ENUM('retrouveur', 'proprietaire', 'mixte') NOT NULL DEFAULT 'mixte',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_created_at (created_at),
    INDEX idx_users_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V2__Create_oauth_accounts_table.sql
CREATE TABLE oauth_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    provider VARCHAR(40) NOT NULL,
    provider_user_id VARCHAR(190) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    
    UNIQUE KEY ux_oauth_provider_user (provider, provider_user_id),
    INDEX idx_oauth_user (user_id),
    
    CONSTRAINT fk_oauth_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V3__Create_listings_table.sql
CREATE TABLE listings (
    id CHAR(36) NOT NULL PRIMARY KEY,
    finder_user_id CHAR(36) NOT NULL,
    title VARCHAR(180) NOT NULL,
    category ENUM('cles', 'electronique', 'bagagerie', 'documents', 'autre') NOT NULL,
    location_text VARCHAR(255) NOT NULL,
    latitude DECIMAL(9,6) NULL,
    longitude DECIMAL(9,6) NULL,
    found_at DATETIME(3) NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(512) NULL,
    status ENUM('active', 'resolu', 'suspendu', 'supprime') NOT NULL DEFAULT 'active',
    views_count BIGINT NOT NULL DEFAULT 0,
    is_moderated BOOLEAN NOT NULL DEFAULT FALSE,
    moderated_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    
    INDEX idx_listings_finder (finder_user_id),
    INDEX idx_listings_category (category),
    INDEX idx_listings_status (status),
    INDEX idx_listings_found_at (found_at),
    INDEX idx_listings_created_at (created_at),
    INDEX idx_listings_location (latitude, longitude),
    INDEX idx_listings_moderated (is_moderated),
    
    FULLTEXT INDEX ftx_listings_text (title, description),
    
    CONSTRAINT fk_listings_finder 
        FOREIGN KEY (finder_user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V4__Create_listing_images_table.sql
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

-- V5__Create_threads_table.sql
CREATE TABLE threads (
    id CHAR(36) NOT NULL PRIMARY KEY,
    listing_id CHAR(36) NOT NULL,
    owner_user_id CHAR(36) NOT NULL,
    finder_user_id CHAR(36) NOT NULL,
    status ENUM('pending', 'approved', 'closed', 'cancelled') NOT NULL DEFAULT 'pending',
    approved_by_owner BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by_finder BOOLEAN NOT NULL DEFAULT FALSE,
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

-- V6__Create_messages_table.sql
CREATE TABLE messages (
    id CHAR(36) NOT NULL PRIMARY KEY,
    thread_id CHAR(36) NOT NULL,
    sender_user_id CHAR(36) NOT NULL,
    body TEXT NOT NULL,
    message_type ENUM('text', 'image', 'system') NOT NULL DEFAULT 'text',
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

-- V7__Create_confirmations_table.sql
CREATE TABLE confirmations (
    id CHAR(36) NOT NULL PRIMARY KEY,
    thread_id CHAR(36) NOT NULL,
    code VARCHAR(12) NOT NULL,
    expires_at DATETIME(3) NOT NULL,
    used_at DATETIME(3) NULL,
    used_by_user_id CHAR(36) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    
    UNIQUE KEY ux_confirmations_thread (thread_id),
    INDEX idx_confirmations_code (code),
    INDEX idx_confirmations_expires (expires_at),
    
    CONSTRAINT fk_confirmations_thread 
        FOREIGN KEY (thread_id) REFERENCES threads(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_confirmations_used_by 
        FOREIGN KEY (used_by_user_id) REFERENCES users(id) 
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V8__Create_alerts_table.sql
CREATE TABLE alerts (
    id CHAR(36) NOT NULL PRIMARY KEY,
    owner_user_id CHAR(36) NOT NULL,
    title VARCHAR(180) NOT NULL,
    query_text VARCHAR(255) NULL,
    category VARCHAR(40) NULL,
    location_text VARCHAR(255) NULL,
    latitude DECIMAL(9,6) NULL,
    longitude DECIMAL(9,6) NULL,
    radius_km DECIMAL(6,2) NULL DEFAULT 10.00,
    date_from DATE NULL,
    date_to DATE NULL,
    channels JSON NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_triggered_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    
    INDEX idx_alerts_owner (owner_user_id),
    INDEX idx_alerts_active (active),
    INDEX idx_alerts_category (category),
    INDEX idx_alerts_location (latitude, longitude),
    
    CONSTRAINT fk_alerts_owner 
        FOREIGN KEY (owner_user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V9__Create_moderation_flags_table.sql
CREATE TABLE moderation_flags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type ENUM('listing', 'message', 'user') NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT NULL,
    status ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
    priority ENUM('low', 'medium', 'high', 'urgent') NOT NULL DEFAULT 'medium',
    created_by_user_id CHAR(36) NULL,
    reviewed_by_user_id CHAR(36) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    reviewed_at DATETIME(3) NULL,
    
    INDEX idx_flags_entity (entity_type, entity_id),
    INDEX idx_flags_status (status),
    INDEX idx_flags_priority (priority),
    INDEX idx_flags_created_at (created_at),
    
    CONSTRAINT fk_flags_creator 
        FOREIGN KEY (created_by_user_id) REFERENCES users(id) 
        ON DELETE SET NULL,
    CONSTRAINT fk_flags_reviewer 
        FOREIGN KEY (reviewed_by_user_id) REFERENCES users(id) 
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V10__Create_push_subscriptions_table.sql
CREATE TABLE push_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    endpoint VARCHAR(512) NOT NULL,
    p256dh_key VARCHAR(255) NOT NULL,
    auth_key VARCHAR(255) NOT NULL,
    user_agent VARCHAR(512) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    last_used_at DATETIME(3) NULL,
    
    UNIQUE KEY ux_push_endpoint (endpoint),
    INDEX idx_push_user (user_id),
    
    CONSTRAINT fk_push_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V11__Create_notification_logs_table.sql
CREATE TABLE notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    type ENUM('email', 'sms', 'push', 'in_app') NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status ENUM('pending', 'sent', 'delivered', 'failed') NOT NULL DEFAULT 'pending',
    provider VARCHAR(50) NULL,
    external_id VARCHAR(255) NULL,
    error_message TEXT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    sent_at DATETIME(3) NULL,
    delivered_at DATETIME(3) NULL,
    
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_created_at (created_at),
    
    CONSTRAINT fk_notifications_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V12__Create_audit_logs_table.sql
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id CHAR(36) NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    action ENUM('CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT') NOT NULL,
    old_values JSON NULL,
    new_values JSON NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(512) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created_at (created_at),
    
    CONSTRAINT fk_audit_user 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
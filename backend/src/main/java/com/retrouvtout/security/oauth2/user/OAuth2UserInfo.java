// OAuth2UserInfo.java
package com.retrouvtout.security.oauth2.user;

import java.util.Map;

/**
 * Classe abstraite pour les informations utilisateur OAuth2
 */
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getImageUrl();
}
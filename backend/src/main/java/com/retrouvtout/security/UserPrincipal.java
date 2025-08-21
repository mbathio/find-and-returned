package com.retrouvtout.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.retrouvtout.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Classe principale pour l'utilisateur dans Spring Security
 * Rôles STRICTEMENT conformes au cahier des charges
 */
public class UserPrincipal implements UserDetails {
    
    protected String id;
    protected String name;
    protected String email;
    
    @JsonIgnore
    protected String password;
    
    protected Collection<? extends GrantedAuthority> authorities;
    protected boolean active;
    protected boolean emailVerified;

    // Constructeur par défaut protégé pour les sous-classes
    protected UserPrincipal() {}

    public UserPrincipal(String id, String name, String email, String password, 
                        Collection<? extends GrantedAuthority> authorities, 
                        boolean active, boolean emailVerified) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.active = active;
        this.emailVerified = emailVerified;
    }

    /**
     * Créer un UserPrincipal à partir d'un User
     * Rôles STRICTEMENT conformes au cahier des charges - Section 3.1
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Ajouter le rôle de base pour tous les utilisateurs authentifiés
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // Ajouter des rôles spécifiques selon le type d'utilisateur
        // UNIQUEMENT les deux rôles définis dans le cahier des charges
        switch (user.getRole()) {
            case RETROUVEUR:
                authorities.add(new SimpleGrantedAuthority("ROLE_FINDER"));
                break;
            case PROPRIETAIRE:
                authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
                break;
        }

        return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities,
                user.getActive(),
                user.getEmailVerified()
        );
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
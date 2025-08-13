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
 */
public class UserPrincipal implements UserDetails {
    
    private String id;
    private String name;
    private String email;
    
    @JsonIgnore
    private String password;
    
    private Collection<? extends GrantedAuthority> authorities;
    private boolean active;
    private boolean emailVerified;

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

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Ajouter le rôle de base
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // Ajouter des rôles spécifiques selon le type d'utilisateur
        switch (user.getRole()) {
            case RETROUVEUR:
                authorities.add(new SimpleGrantedAuthority("ROLE_FINDER"));
                break;
            case PROPRIETAIRE:
                authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
                break;
            case MIXTE:
                authorities.add(new SimpleGrantedAuthority("ROLE_FINDER"));
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
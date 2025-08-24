package com.retrouvtout.security;

import com.retrouvtout.entity.User;
import com.retrouvtout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service personnalisé pour charger les détails de l'utilisateur pour Spring Security
 * Rôles STRICTEMENT conformes au cahier des charges
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(String id) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> 
                    new UsernameNotFoundException("Utilisateur non trouvé avec l'ID : " + id));

        return UserPrincipal.create(user);
    }

    /**
     * ✅ CORRECTION : Convertit le rôle de l'utilisateur en autorités Spring Security avec MIXTE
     * UNIQUEMENT les rôles conformes au cahier des charges
     */
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(User.UserRole role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Ajouter le rôle de base pour tous les utilisateurs authentifiés
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // ✅ CORRECTION : Ajouter des rôles spécifiques selon le type d'utilisateur avec MIXTE
        switch (role) {
            case RETROUVEUR:
                authorities.add(new SimpleGrantedAuthority("ROLE_FINDER"));
                break;
            case PROPRIETAIRE:
                authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
                break;
            case MIXTE:
                // ✅ MIXTE a les deux rôles
                authorities.add(new SimpleGrantedAuthority("ROLE_FINDER"));
                authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
                break;
        }
        
        return authorities;
    }
}
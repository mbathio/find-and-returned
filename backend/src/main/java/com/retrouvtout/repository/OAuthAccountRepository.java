// OAuthAccountRepository.java
package com.retrouvtout.repository;

import com.retrouvtout.entity.OAuthAccount;
import com.retrouvtout.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    /**
     * Trouver un compte OAuth par fournisseur et ID utilisateur
     */
    Optional<OAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    /**
     * Trouver tous les comptes OAuth d'un utilisateur
     */
    List<OAuthAccount> findByUser(User user);

    /**
     * Trouver les comptes OAuth par fournisseur
     */
    List<OAuthAccount> findByProvider(String provider);

    /**
     * Vérifier si un utilisateur a un compte OAuth pour un fournisseur
     */
    boolean existsByUserAndProvider(User user, String provider);
}
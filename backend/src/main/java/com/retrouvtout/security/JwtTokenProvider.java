// backend/src/main/java/com/retrouvtout/security/JwtTokenProvider.java - DEBUG VERSION

package com.retrouvtout.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ✅ FOURNISSEUR JWT avec DEBUG MAXIMAL pour résoudre les problèmes d'auth
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationInMs;

    /**
     * ✅ Générer un token d'accès JWT avec debug
     */
    public String generateToken(String userId) {
        try {
            System.out.println("🔧 JWT - Génération token pour userId: " + userId);
            
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("UserID ne peut pas être null ou vide");
            }
            
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            String token = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

            System.out.println("✅ Token JWT généré avec succès");
            System.out.println("  - UserID: " + userId);
            System.out.println("  - Expiration: " + expiryDate);
            System.out.println("  - Token (20 premiers chars): " + token.substring(0, Math.min(token.length(), 20)) + "...");
            
            return token;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de générer le token JWT", e);
        }
    }

    /**
     * ✅ Extraire l'ID utilisateur depuis le token JWT avec debug
     */
    public String getUserIdFromToken(String token) {
        try {
            System.out.println("🔧 JWT - Extraction userId du token");
            System.out.println("  - Token (20 premiers chars): " + token.substring(0, Math.min(token.length(), 20)) + "...");
            
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

            String userId = claims.getSubject();
            
            if (userId == null || userId.trim().isEmpty()) {
                System.err.println("❌ Subject (userId) manquant dans le token");
                throw new IllegalArgumentException("Token invalide: subject manquant");
            }
            
            System.out.println("✅ UserID extrait avec succès: " + userId);
            return userId;
            
        } catch (ExpiredJwtException e) {
            System.err.println("❌ Token expiré");
            System.err.println("  - Expiration: " + e.getClaims().getExpiration());
            System.err.println("  - Maintenant: " + new Date());
            throw new RuntimeException("Token expiré", e);
        } catch (UnsupportedJwtException e) {
            System.err.println("❌ Token non supporté: " + e.getMessage());
            throw new RuntimeException("Token non supporté", e);
        } catch (MalformedJwtException e) {
            System.err.println("❌ Token malformé: " + e.getMessage());
            throw new RuntimeException("Token malformé", e);
        } catch (SecurityException e) {
            System.err.println("❌ Signature invalide: " + e.getMessage());
            throw new RuntimeException("Signature de token invalide", e);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Token vide ou null: " + e.getMessage());
            throw new RuntimeException("Token vide", e);
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de l'extraction: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'analyse du token", e);
        }
    }

    /**
     * ✅ Valider le token JWT avec debug détaillé
     */
    public boolean validateToken(String authToken) {
        try {
            System.out.println("🔧 JWT - Validation du token");
            System.out.println("  - Token (20 premiers chars): " + authToken.substring(0, Math.min(authToken.length(), 20)) + "...");
            
            if (authToken == null || authToken.trim().isEmpty()) {
                System.err.println("❌ Token null ou vide");
                return false;
            }
            
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken)
                .getBody();
            
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            System.out.println("✅ Token validé avec succès");
            System.out.println("  - Subject: " + claims.getSubject());
            System.out.println("  - Issued At: " + claims.getIssuedAt());
            System.out.println("  - Expiration: " + expiration);
            System.out.println("  - Now: " + now);
            System.out.println("  - Is Expired: " + expiration.before(now));
            
            return true;
            
        } catch (ExpiredJwtException e) {
            System.err.println("❌ Token expiré lors de la validation");
            System.err.println("  - Expiration: " + e.getClaims().getExpiration());
            System.err.println("  - Maintenant: " + new Date());
            return false;
        } catch (UnsupportedJwtException e) {
            System.err.println("❌ Token non supporté lors de la validation: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.err.println("❌ Token malformé lors de la validation: " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            System.err.println("❌ Signature invalide lors de la validation: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Argument invalide lors de la validation: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ✅ Générer un refresh token JWT
     */
    public String generateRefreshToken(String userId) {
        try {
            System.out.println("🔧 JWT - Génération refresh token pour userId: " + userId);
            
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            String token = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

            System.out.println("✅ Refresh token généré avec succès pour utilisateur: " + userId);
            
            return token;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du refresh token: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de générer le refresh token", e);
        }
    }

    /**
     * ✅ Obtenir la date d'expiration du token
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

            return claims.getExpiration();
            
        } catch (Exception e) {
            System.err.println("❌ Impossible d'obtenir l'expiration: " + e.getMessage());
            return null;
        }
    }

    /**
     * ✅ Vérifier si le token est expiré
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return true;
        }
        boolean isExpired = expiration.before(new Date());
        System.out.println("🔧 JWT - Token expiré? " + isExpired + " (expire le " + expiration + ")");
        return isExpired;
    }

    /**
     * ✅ Méthode de debug pour analyser un token
     */
    public void debugToken(String token) {
        try {
            System.out.println("🔍 JWT DEBUG - Analyse complète du token");
            System.out.println("  - Longueur: " + token.length());
            System.out.println("  - Début: " + token.substring(0, Math.min(token.length(), 50)) + "...");
            
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            System.out.println("  - Subject: " + claims.getSubject());
            System.out.println("  - Issued At: " + claims.getIssuedAt());
            System.out.println("  - Expiration: " + claims.getExpiration());
            System.out.println("  - All Claims: " + claims);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur debug token: " + e.getMessage());
        }
    }
}
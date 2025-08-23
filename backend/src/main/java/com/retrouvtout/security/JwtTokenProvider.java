// backend/src/main/java/com/retrouvtout/security/JwtTokenProvider.java
package com.retrouvtout.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ✅ FOURNISSEUR JWT CORRIGÉ - Gère la création et validation des tokens JWT
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
     * ✅ Générer un token d'accès JWT
     */
    public String generateToken(String userId) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            String token = Jwts.builder()
                .setSubject(userId) // ✅ L'ID utilisateur comme subject
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

            System.out.println("✅ Token JWT généré pour utilisateur: " + userId);
            System.out.println("✅ Expiration: " + expiryDate);
            
            return token;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du token: " + e.getMessage());
            throw new RuntimeException("Impossible de générer le token JWT", e);
        }
    }

    /**
     * ✅ Générer un refresh token JWT (plus longue durée)
     */
    public String generateRefreshToken(String userId) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            String token = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh") // ✅ Marquer comme refresh token
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

            System.out.println("✅ Refresh token généré pour utilisateur: " + userId);
            
            return token;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du refresh token: " + e.getMessage());
            throw new RuntimeException("Impossible de générer le refresh token", e);
        }
    }

    /**
     * ✅ Extraire l'ID utilisateur depuis le token JWT
     */
    public String getUserIdFromToken(String token) {
        try {
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
            
            System.out.println("✅ UserID extrait du token: " + userId);
            return userId;
            
        } catch (ExpiredJwtException e) {
            System.err.println("❌ Token expiré: " + e.getMessage());
            throw new RuntimeException("Token expiré", e);
        } catch (UnsupportedJwtException e) {
            System.err.println("❌ Token non supporté: " + e.getMessage());
            throw new RuntimeException("Token non supporté", e);
        } catch (MalformedJwtException e) {
            System.err.println("❌ Token malformé: " + e.getMessage());
            throw new RuntimeException("Token malformé", e);
        } catch (SignatureException e) {
            System.err.println("❌ Signature invalide: " + e.getMessage());
            throw new RuntimeException("Signature de token invalide", e);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Token vide: " + e.getMessage());
            throw new RuntimeException("Token vide", e);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'extraction de l'utilisateur: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'analyse du token", e);
        }
    }

    /**
     * ✅ Valider le token JWT
     */
    public boolean validateToken(String authToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken);
            
            System.out.println("✅ Token validé avec succès");
            return true;
            
        } catch (ExpiredJwtException e) {
            System.err.println("❌ Token expiré: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("❌ Token non supporté: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("❌ Token malformé: " + e.getMessage());
        } catch (SignatureException e) {
            System.err.println("❌ Signature invalide: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Argument invalide: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur de validation: " + e.getMessage());
        }
        
        return false;
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
        return expiration.before(new Date());
    }

    /**
     * ✅ Générer un token de vérification d'email (24h de validité)
     */
    public String generateEmailVerificationToken(String userId) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + (24 * 60 * 60 * 1000)); // 24 heures

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            String token = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "email_verification") // ✅ Marquer comme token de vérification
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

            System.out.println("✅ Token de vérification email généré pour utilisateur: " + userId);
            
            return token;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du token de vérification: " + e.getMessage());
            throw new RuntimeException("Impossible de générer le token de vérification", e);
        }
    }
}
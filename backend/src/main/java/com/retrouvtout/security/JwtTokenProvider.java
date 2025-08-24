package com.retrouvtout.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ✅ FOURNISSEUR JWT avec logs modérés pour la production
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

            System.out.println("✅ Token JWT généré pour userId: " + userId);
            return token;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur génération token: " + e.getMessage());
            throw new RuntimeException("Impossible de générer le token JWT", e);
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
                throw new IllegalArgumentException("Token invalide: subject manquant");
            }
            
            return userId;
            
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expiré", e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Token non supporté", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Token malformé", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Signature de token invalide", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Token vide", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'analyse du token", e);
        }
    }

    /**
     * ✅ Valider le token JWT
     */
    public boolean validateToken(String authToken) {
        try {
            if (authToken == null || authToken.trim().isEmpty()) {
                return false;
            }
            
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken);
            
            return true;
            
        } catch (ExpiredJwtException e) {
            System.err.println("❌ Token expiré");
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            System.err.println("❌ Token invalide: " + e.getClass().getSimpleName());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur validation token: " + e.getMessage());
            return false;
        }
    }

    /**
     * ✅ Générer un refresh token JWT
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
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

            System.out.println("✅ Refresh token généré pour userId: " + userId);
            return token;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur génération refresh token: " + e.getMessage());
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
}
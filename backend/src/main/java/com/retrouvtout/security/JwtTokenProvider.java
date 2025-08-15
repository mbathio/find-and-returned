package com.retrouvtout.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Fournisseur de tokens JWT pour l'authentification
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationInMs;

    private static final String ISSUER = "retrouvtout-api";
    private static final String AUDIENCE = "retrouvtout-app";

    /**
     * Générer un token d'accès
     */
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("type", "access")
                .sign(algorithm);
    }

    /**
     * Générer un token de rafraîchissement
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("type", "refresh")
                .sign(algorithm);
    }

    /**
     * Générer un token de réinitialisation de mot de passe
     */
    public String generatePasswordResetToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (60 * 60 * 1000)); // 1 heure

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("type", "password_reset")
                .sign(algorithm);
    }

    /**
     * Générer un token de vérification d'email
     */
    public String generateEmailVerificationToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (24 * 60 * 60 * 1000)); // 24 heures

        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(userId)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("type", "email_verification")
                .sign(algorithm);
    }

    /**
     * Extraire l'ID utilisateur du token
     */
    public String getUserIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Token invalide", e);
        }
    }

    /**
     * Obtenir la date d'expiration du token
     */
    public Date getExpirationFromToken(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getExpiresAt();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Token invalide", e);
        }
    }

    /**
     * Valider un token
     */
    public boolean validateToken(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    /**
     * Vérifier et décoder un token
     */
    private DecodedJWT verifyToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .build();

        return verifier.verify(token);
    }

    /**
     * Obtenir le type de token
     */
    public String getTokenType(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getClaim("type").asString();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Token invalide", e);
        }
    }

    /**
     * Vérifier si le token est du type spécifié
     */
    public boolean isTokenOfType(String token, String type) {
        try {
            String tokenType = getTokenType(token);
            return type.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
}
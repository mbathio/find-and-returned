package com.retrouvtout.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import java.io.IOException;

/**
 * ✅ FILTRE JWT CORRIGÉ avec debugging et gestion d'erreur améliorée
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        try {
            // ✅ DEBUG: Log pour tracer les requêtes
            System.out.println("🔍 JWT Filter - " + method + " " + requestPath);
            
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                System.out.println("✅ Token trouvé dans la requête");
                
                // ✅ VALIDATION: Vérifier si le token est valide
                if (tokenProvider.validateToken(jwt)) {
                    System.out.println("✅ Token valide");
                    
                    try {
                        String userId = tokenProvider.getUserIdFromToken(jwt);
                        System.out.println("✅ UserID extrait: " + userId);

                        UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                        System.out.println("✅ UserDetails chargé pour: " + userDetails.getUsername());

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("✅ Authentification définie dans SecurityContext");
                        
                    } catch (Exception userError) {
                        System.err.println("❌ Erreur lors du chargement utilisateur: " + userError.getMessage());
                        // ✅ IMPORTANT: Nettoyer le contexte en cas d'erreur
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    System.err.println("❌ Token JWT invalide pour " + requestPath);
                    // ✅ IMPORTANT: Nettoyer le contexte si token invalide
                    SecurityContextHolder.clearContext();
                }
            } else {
                System.out.println("ℹ️ Aucun token JWT trouvé pour " + requestPath);
            }
        } catch (Exception ex) {
            System.err.println("❌ Erreur dans JwtAuthenticationFilter pour " + requestPath + ": " + ex.getMessage());
            ex.printStackTrace();
            
            // ✅ CRITIQUE: Toujours nettoyer le contexte en cas d'erreur
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * ✅ MÉTHODE AMÉLIORÉE pour extraire le JWT de la requête
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // ✅ DEBUG: Log de l'header Authorization
        if (bearerToken != null) {
            System.out.println("🔍 Authorization header: " + bearerToken.substring(0, Math.min(bearerToken.length(), 20)) + "...");
        }
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            
            // ✅ VALIDATION: Vérifier que le token n'est pas vide après extraction
            if (token.trim().isEmpty()) {
                System.err.println("❌ Token vide après extraction de 'Bearer '");
                return null;
            }
            
            return token;
        }
        
        return null;
    }

    /**
     * ✅ MÉTHODE pour sauter le filtre sur certains endpoints
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // ✅ Endpoints publics qui ne nécessitent pas d'authentification
        boolean isPublicEndpoint = path.startsWith("/api/auth/") ||
                                  path.startsWith("/api/test") ||
                                  path.startsWith("/api/health") ||
                                  path.startsWith("/api/cors-test") ||
                                  path.startsWith("/api/debug") ||
                                  path.startsWith("/api/db-test") ||
                                  path.startsWith("/actuator/") ||
                                  path.startsWith("/swagger-ui/") ||
                                  path.startsWith("/v3/api-docs/") ||
                                  path.startsWith("/api-docs/") ||
                                  path.equals("/") ||
                                  path.equals("/health") ||
                                  (path.startsWith("/api/listings") && "GET".equals(method)); // GET listings public
        
        if (isPublicEndpoint) {
            System.out.println("⏭️ Filtrage JWT ignoré pour endpoint public: " + method + " " + path);
        }
        
        return isPublicEndpoint;
    }
}
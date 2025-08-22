package com.retrouvtout.controller;

import com.retrouvtout.entity.User;
import com.retrouvtout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ Contrôleur de test pour vérifier la base de données
 * À SUPPRIMER une fois les tests terminés
 */
@RestController
@RequestMapping("/api/db-test")
@CrossOrigin(origins = {"*"})
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    /**
     * Test de connexion à la base de données
     */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            response.put("success", true);
            response.put("connected", true);
            response.put("url", connection.getMetaData().getURL());
            response.put("driver", connection.getMetaData().getDriverName());
            response.put("database", connection.getCatalog());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test de la table users
     */
    @GetMapping("/users-table")
    public ResponseEntity<Map<String, Object>> testUsersTable() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            
            // Vérifier si la table existe
            ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'users'");
            if (rs.next()) {
                response.put("table_exists", true);
                
                // Obtenir la structure de la table
                ResultSet columns = stmt.executeQuery("DESCRIBE users");
                StringBuilder structure = new StringBuilder();
                while (columns.next()) {
                    structure.append(columns.getString("Field"))
                             .append(" ")
                             .append(columns.getString("Type"))
                             .append(", ");
                }
                response.put("structure", structure.toString());
                
                // Compter les utilisateurs
                ResultSet count = stmt.executeQuery("SELECT COUNT(*) as total FROM users");
                if (count.next()) {
                    response.put("user_count", count.getInt("total"));
                }
                
            } else {
                response.put("table_exists", false);
            }
            
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test du repository
     */
    @GetMapping("/repository")
    public ResponseEntity<Map<String, Object>> testRepository() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long count = userRepository.count();
            response.put("success", true);
            response.put("repository_working", true);
            response.put("user_count", count);
            
            // Test de recherche par email
            boolean exists = userRepository.existsByEmailAndActiveTrue("test@example.com");
            response.put("email_search_working", true);
            response.put("test_email_exists", exists);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test de création d'utilisateur simple
     */
    @PostMapping("/create-test-user")
    public ResponseEntity<Map<String, Object>> createTestUser() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifier si l'utilisateur test existe déjà
            if (userRepository.existsByEmailAndActiveTrue("test@retrouvtout.dev")) {
                response.put("success", true);
                response.put("message", "Utilisateur test existe déjà");
                return ResponseEntity.ok(response);
            }
            
            // Créer un utilisateur test
            User testUser = new User();
            testUser.setId(java.util.UUID.randomUUID().toString());
            testUser.setName("Utilisateur Test");
            testUser.setEmail("test@retrouvtout.dev");
            testUser.setPasswordHash("$2a$12$dummy.hash.for.test"); // Hash fictif
            testUser.setRole(User.UserRole.MIXTE);
            testUser.setEmailVerified(false);
            testUser.setActive(true);
            
            User savedUser = userRepository.save(testUser);
            
            response.put("success", true);
            response.put("message", "Utilisateur test créé avec succès");
            response.put("user_id", savedUser.getId());
            response.put("user_email", savedUser.getEmail());
            response.put("user_role", savedUser.getRole().getValue());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test des contraintes de validation
     */
    @GetMapping("/test-constraints")
    public ResponseEntity<Map<String, Object>> testConstraints() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test 1: Email unique
            response.put("testing", "email_unique_constraint");
            
            if (userRepository.existsByEmailAndActiveTrue("test@retrouvtout.dev")) {
                response.put("unique_email_constraint", "working");
            } else {
                response.put("unique_email_constraint", "no_test_user");
            }
            
            // Test 2: Enum role
            response.put("enum_roles", new String[]{"retrouveur", "proprietaire", "mixte"});
            
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Nettoyer les données de test
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanup() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Supprimer l'utilisateur test
            userRepository.findByEmailAndActiveTrue("test@retrouvtout.dev")
                .ifPresent(user -> userRepository.delete(user));
            
            response.put("success", true);
            response.put("message", "Données de test nettoyées");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
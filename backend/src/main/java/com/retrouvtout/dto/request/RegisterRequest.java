package com.retrouvtout.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 120, message = "Le nom ne peut pas dépasser 120 caractères")
    private String name;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 190, message = "L'email ne peut pas dépasser 190 caractères")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, max = 255, message = "Le mot de passe doit contenir entre 6 et 255 caractères")
    private String password;
    
    @Size(max = 40, message = "Le numéro de téléphone ne peut pas dépasser 40 caractères")
    private String phone;
    
    // ✅ CORRECTION: Rôle optionnel avec valeur par défaut
    private String role; // Peut être null, sera défini à "mixte" par défaut
    
    // Constructeurs
    public RegisterRequest() {}
    
    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = "mixte"; // Valeur par défaut
    }
    
    // Getters et Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getRole() { 
        // ✅ Retourner "mixte" par défaut si null ou vide
        return (role == null || role.trim().isEmpty()) ? "mixte" : role; 
    }
    public void setRole(String role) { this.role = role; }
    
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + getRole() + '\'' +
                '}';
    }
}
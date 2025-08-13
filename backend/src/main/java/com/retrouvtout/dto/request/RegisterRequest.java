// RegisterRequest.java
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
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;
    
    @Size(max = 40, message = "Le numéro de téléphone ne peut pas dépasser 40 caractères")
    private String phone;
    
    private String role = "mixte"; // Par défaut
    
    // Constructeurs
    public RegisterRequest() {}
    
    // Getters et Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
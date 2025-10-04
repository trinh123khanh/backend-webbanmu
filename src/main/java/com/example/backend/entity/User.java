package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;
import java.util.HashSet;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    private String fullName;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new HashSet<>();
    
    public enum UserRole {
        USER, ADMIN
    }
    
    // Helper method to get roles as a Set of SimpleGrantedAuthority
    public Set<UserRole> getRoles() {
        return roles;
    }
    
    // Add a role to the user
    public void addRole(UserRole role) {
        this.roles.add(role);
    }
    
    // Check if user has a specific role
    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }
}

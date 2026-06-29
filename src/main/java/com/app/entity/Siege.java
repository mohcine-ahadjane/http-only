package com.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "siege")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Siege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String nom;

    @Column(length = 255)
    private String adresse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "siege", fetch = FetchType.LAZY)
    private List<AppUser> users = new ArrayList<>();

    @OneToMany(mappedBy = "siege", fetch = FetchType.LAZY)
    private List<Depot> depots = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

package com.gestrh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "departements")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // DB column nom_departement
    @Column(name = "nom_departement", nullable = false, length = 255)
    private String nom;

    // Optional manager/responsable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // --- Getters/Setters (canonical) ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    /** Canonical name field (maps nom_departement) */
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Utilisateur getResponsable() { return responsable; }
    public void setResponsable(Utilisateur responsable) { this.responsable = responsable; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // --- Backward-compat (if some JSP/servlet uses nomDepartement) ---
    public String getNomDepartement() { return getNom(); }
    public void setNomDepartement(String val) { setNom(val); }

    // Convenience
    @Transient
    public boolean isDeleted() { return deletedAt != null; }
}

package com.gestrh.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendrier_evenements")
public class CalendrierEvenement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Adapt column names here if your table uses different names
    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    @Column(name = "description")   // if your column is "notes", change this to name="notes"
    private String description;

    @Column(name = "date_evenement", nullable = false)  // DATE (no time)
    private LocalDate dateEvenement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par")  // FK -> utilisateurs.id
    private Utilisateur creePar;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters/Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDate dateEvenement) { this.dateEvenement = dateEvenement; }

    public Utilisateur getCreePar() { return creePar; }
    public void setCreePar(Utilisateur creePar) { this.creePar = creePar; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Back-compat aliases if needed elsewhere
    @Transient public String getNotes() { return getDescription(); }
    @Transient public void setNotes(String v) { setDescription(v); }
}

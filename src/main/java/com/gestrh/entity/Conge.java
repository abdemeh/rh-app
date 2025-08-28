package com.gestrh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conges")
public class Conge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private CongeType type;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "nb_jours", precision = 5, scale = 2, nullable = false)
    private BigDecimal nbJours;

    // enum in DB; we map as String
    @Column(name = "statut", length = 20, nullable = false)
    private String statut = "en_attente";

    @Column(name = "motif", length = 255)
    private String motif;

    @Column(name = "created_at", columnDefinition = "datetime")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "datetime")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "datetime")
    private LocalDateTime deletedAt;

    @Column(name = "justificatif_path")
    private String justificatifPath;
    @Column(name = "justificatif_name")
    private String justificatifName;
    @Column(name = "justificatif_type")
    private String justificatifType;

    public Conge() {}

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    @PreUpdate
    public void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Integer getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public CongeType getType() { return type; }
    public void setType(CongeType type) { this.type = type; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public BigDecimal getNbJours() { return nbJours; }
    public void setNbJours(BigDecimal nbJours) { this.nbJours = nbJours; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public String getJustificatifPath() { return justificatifPath; }
    public void setJustificatifPath(String justificatifPath) { this.justificatifPath = justificatifPath; }
    public String getJustificatifName() { return justificatifName; }
    public void setJustificatifName(String justificatifName) { this.justificatifName = justificatifName; }
    public String getJustificatifType() { return justificatifType; }
    public void setJustificatifType(String justificatifType) { this.justificatifType = justificatifType; }
}

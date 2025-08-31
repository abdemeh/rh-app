package com.gestrh.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "presences",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_presence_user_day", columnNames = {"utilisateur_id", "jour"})
        })
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // FK -> utilisateurs.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "jour", nullable = false)
    private LocalDate jour;

    @Column(name = "heure_entree")
    private LocalTime heureEntree;

    @Column(name = "heure_sortie")
    private LocalTime heureSortie;

    @Column(name = "statut", length = 20)
    private String statut;

    @Column(name = "commentaire")
    private String commentaire;

    // Colonne générée historique (read-only) — on ne s'en sert pas pour les totaux
    @Column(name = "duree_minutes", insertable = false, updatable = false)
    private Integer dureeMinutes;

    // Multi-créneaux
    @OneToMany(mappedBy = "presence", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime ASC, id ASC")
    private List<PresenceInterval> intervals;

    // Getters / Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public LocalDate getJour() { return jour; }
    public void setJour(LocalDate jour) { this.jour = jour; }

    public LocalTime getHeureEntree() { return heureEntree; }
    public void setHeureEntree(LocalTime heureEntree) { this.heureEntree = heureEntree; }

    public LocalTime getHeureSortie() { return heureSortie; }
    public void setHeureSortie(LocalTime heureSortie) { this.heureSortie = heureSortie; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Integer getDureeMinutes() { return dureeMinutes; } // read-only

    public List<PresenceInterval> getIntervals() { return intervals; }
    public void setIntervals(List<PresenceInterval> intervals) { this.intervals = intervals; }
}

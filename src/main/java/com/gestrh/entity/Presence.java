package com.gestrh.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity(name = "Presence")                 // <-- JPQL entity name
@Table(name = "presences")                 // <-- DB table
public class Presence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "jour", nullable = false)
    private LocalDate jour;

    @Column(name = "heure_entree")
    private LocalTime heureEntree;

    @Column(name = "heure_sortie")
    private LocalTime heureSortie;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut;                 // present/absent/retard/teletravail

    @Column(name = "commentaire")
    private String commentaire;

    // Generated column in DB; mark as read-only
    @Column(name = "duree_minutes", insertable = false, updatable = false)
    private Integer dureeMinutes;

    public Presence() {}

    // getters
    public Integer getId() { return id; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public LocalDate getJour() { return jour; }
    public LocalTime getHeureEntree() { return heureEntree; }
    public LocalTime getHeureSortie() { return heureSortie; }
    public String getStatut() { return statut; }
    public String getCommentaire() { return commentaire; }
    public Integer getDureeMinutes() { return dureeMinutes; }

    // setters
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public void setJour(LocalDate jour) { this.jour = jour; }
    public void setHeureEntree(LocalTime heureEntree) { this.heureEntree = heureEntree; }
    public void setHeureSortie(LocalTime heureSortie) { this.heureSortie = heureSortie; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}

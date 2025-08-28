package com.gestrh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100) private String nom;
    @Column(nullable = false, length = 100) private String prenom;
    @Column(nullable = false, length = 120, unique = true) private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "poste_id")
    private Poste poste;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "departement_id")
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "manager_id")
    private Utilisateur manager;

    @Column private String adresse;
    @Column private String telephone;

    @Column(nullable = false, length = 20) private String statut = "actif";

    @Column(name = "contrat_type", length = 20) private String contratType;

    @Column(name = "date_embauche") private LocalDate dateEmbauche;
    @Column(name = "date_sortie") private LocalDate dateSortie;

    @Column(name = "salaire_base", precision = 10, scale = 2) private BigDecimal salaireBase;

    @Column(name = "date_creation", columnDefinition = "datetime")
    private LocalDateTime dateCreation;

    @Column(name = "last_login", columnDefinition = "datetime")
    private LocalDateTime lastLogin;

    public Utilisateur() {}

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }
    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }
    public Utilisateur getManager() { return manager; }
    public void setManager(Utilisateur manager) { this.manager = manager; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getContratType() { return contratType; }
    public void setContratType(String contratType) { this.contratType = contratType; }
    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDate dateEmbauche) { this.dateEmbauche = dateEmbauche; }
    public LocalDate getDateSortie() { return dateSortie; }
    public void setDateSortie(LocalDate dateSortie) { this.dateSortie = dateSortie; }
    public BigDecimal getSalaireBase() { return salaireBase; }
    public void setSalaireBase(BigDecimal salaireBase) { this.salaireBase = salaireBase; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}

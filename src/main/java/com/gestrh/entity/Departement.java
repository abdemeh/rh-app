package com.gestrh.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departements")
public class Departement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // DB: nom_departement
    @Column(name = "nom_departement", nullable = false, length = 100)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    public Departement() {}

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }    // <-- you needed this setter
    public Utilisateur getResponsable() { return responsable; }
    public void setResponsable(Utilisateur responsable) { this.responsable = responsable; }
}

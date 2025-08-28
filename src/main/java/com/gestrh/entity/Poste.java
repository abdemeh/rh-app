package com.gestrh.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "postes")
public class Poste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // DB column is intitule_poste → map to field "intitule"
    @Column(name = "intitule_poste", nullable = false, length = 100)
    private String intitule;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id", nullable = false)
    private Departement departement;

    public Poste() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }
}

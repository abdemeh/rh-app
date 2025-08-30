package com.gestrh.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "conges_types")
public class CongeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "libelle", nullable = false, length = 255)
    private String libelle;

    @Column(name = "max_jours_an")
    private Integer maxJoursAn;

    @Column(name = "approval_levels")
    private Integer approvalLevels;

    @Column(name = "requires_doc")
    private Boolean requiresDoc;

    @Column(name = "actif")
    private Boolean actif;

    // ---------- Canonical getters/setters ----------
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Integer getMaxJoursAn() { return maxJoursAn; }
    public void setMaxJoursAn(Integer maxJoursAn) { this.maxJoursAn = maxJoursAn; }

    public Integer getApprovalLevels() { return approvalLevels; }
    public void setApprovalLevels(Integer approvalLevels) { this.approvalLevels = approvalLevels; }

    public Boolean getRequiresDoc() { return requiresDoc; }
    public void setRequiresDoc(Boolean requiresDoc) { this.requiresDoc = requiresDoc; }
    public boolean isRequiresDoc() { return Boolean.TRUE.equals(requiresDoc); }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
    public boolean isActif() { return Boolean.TRUE.equals(actif); }

    // ---------- Backward-compat aliases ----------
    // Some code calls these older names; keep them delegating to the real fields.

    /** alias for libellé/intitulé used elsewhere */
    public String getNom() { return getLibelle(); }
    public void setNom(String nom) { setLibelle(nom); }

    public String getIntitule() { return getLibelle(); }
    public void setIntitule(String intitule) { setLibelle(intitule); }

    /** plural/singular variants used in other servlets */
    public Integer getJoursAnnuels() { return getMaxJoursAn(); }
    public void setJoursAnnuels(Integer v) { setMaxJoursAn(v); }
    public Integer getJoursAnnuel() { return getMaxJoursAn(); }
    public void setJoursAnnuel(Integer v) { setMaxJoursAn(v); }

    // No 'description' column in DB → do NOT map; if some code calls getDescription(), add a transient if ever needed.
}

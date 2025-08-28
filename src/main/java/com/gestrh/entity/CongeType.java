package com.gestrh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "conges_types")
public class CongeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(name = "max_jours_an", precision = 5, scale = 2)
    private BigDecimal maxJoursAn;

    @Column(name = "approval_levels", nullable = false)
    private int approvalLevels = 1;

    @Column(name = "requires_doc", nullable = false)
    private boolean requiresDoc = false;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public CongeType() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public BigDecimal getMaxJoursAn() { return maxJoursAn; }
    public void setMaxJoursAn(BigDecimal maxJoursAn) { this.maxJoursAn = maxJoursAn; }

    public int getApprovalLevels() { return approvalLevels; }
    public void setApprovalLevels(int approvalLevels) { this.approvalLevels = approvalLevels; }

    public boolean getRequiresDoc() { return requiresDoc; }   // used in JSP/servlet
    public boolean isRequiresDoc() { return requiresDoc; }
    public void setRequiresDoc(boolean requiresDoc) { this.requiresDoc = requiresDoc; }

    public boolean isActif() { return actif; }
    public boolean getActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}

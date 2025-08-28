package com.gestrh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="conges_approbations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"conge_id","level_idx"}))
public class CongeApprobation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="conge_id", nullable=false)
    private Conge conge;

    @Column(name="level_idx", nullable=false)
    private short levelIdx;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="approbateur_id", nullable=false)
    private Utilisateur approbateur;

    // 'en_attente','approuve','rejete'
    @Column(length=20, nullable=false) private String statut = "en_attente";

    @Column(length=255) private String commentaire;
    @Column(name="decided_at") private LocalDateTime decidedAt;

    public CongeApprobation() {}
    public Integer getId() { return id; }
    public Conge getConge() { return conge; }
    public void setConge(Conge conge) { this.conge = conge; }
    public short getLevelIdx() { return levelIdx; }
    public void setLevelIdx(short levelIdx) { this.levelIdx = levelIdx; }
    public Utilisateur getApprobateur() { return approbateur; }
    public void setApprobateur(Utilisateur approbateur) { this.approbateur = approbateur; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
}

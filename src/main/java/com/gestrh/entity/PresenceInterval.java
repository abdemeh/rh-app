package com.gestrh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "presence_intervals")
public class PresenceInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // aligne avec Presence.id (Integer)

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "presence_id")
    private Presence presence;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Colonne générée par MySQL (read-only)
    @Column(name = "minutes", insertable = false, updatable = false)
    private Integer minutes;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Getters / Setters
    public Integer getId() { return id; }
    public Presence getPresence() { return presence; }
    public void setPresence(Presence presence) { this.presence = presence; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getMinutes() { return minutes; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

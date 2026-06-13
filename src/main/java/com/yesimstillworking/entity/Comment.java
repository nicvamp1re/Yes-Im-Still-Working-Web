package com.yesimstillworking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "patch_id", nullable = false)
    private Patch patch;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true) // Allows orphan preservation
    private User user;

    private String fallbackUsername; // Backup name cached on creation

    public Comment() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Patch getPatch() { return patch; }
    public void setPatch(Patch patch) { this.patch = patch; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFallbackUsername() { return fallbackUsername; }
    public void setFallbackUsername(String fallbackUsername) { this.fallbackUsername = fallbackUsername; }
}
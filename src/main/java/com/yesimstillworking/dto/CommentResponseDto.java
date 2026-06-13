package com.yesimstillworking.dto;

import java.time.LocalDateTime;

public class CommentResponseDto {
    private Long id;
    private Long userId;        // Null if account deleted
    private String username;    // Active or fallback name
    private String text;
    private LocalDateTime createdAt;
    private String patchTitle;  // Needed for user profile history view

    // Must accept exactly 6 arguments!
    public CommentResponseDto(Long id, Long userId, String username, String text, LocalDateTime createdAt, String patchTitle) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.createdAt = createdAt;
        this.patchTitle = patchTitle;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getPatchTitle() { return patchTitle; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setText(String text) { this.text = text; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setPatchTitle(String patchTitle) { this.patchTitle = patchTitle; }
}
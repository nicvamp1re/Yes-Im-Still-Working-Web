package com.yesimstillworking.dto;

import java.time.LocalDate;
import java.util.List;

public class PatchResponseDto {
    private Long id;
    private String title;
    private LocalDate releaseDate;
    private String description;
    private List<CommentResponseDto> comments;

    // Full Constructor
    public PatchResponseDto(Long id, String title, LocalDate releaseDate, String description, List<CommentResponseDto> comments) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.description = description;
        this.comments = comments;
    }

    // Getters (Required by Jackson serializer to build JSON responses)
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public String getDescription() { return description; }
    public List<CommentResponseDto> getComments() { return comments; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public void setDescription(String description) { this.description = description; }
    public void setComments(List<CommentResponseDto> comments) { this.comments = comments; }
}
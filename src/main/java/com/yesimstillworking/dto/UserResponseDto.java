package com.yesimstillworking.dto;

public class UserResponseDto {
    private Long id;
    private String username;

    // Full Constructor
    public UserResponseDto(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    // Getters (Required by Jackson to map out JSON payloads)
    public Long getId() { return id; }
    public String getUsername() { return username; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
}
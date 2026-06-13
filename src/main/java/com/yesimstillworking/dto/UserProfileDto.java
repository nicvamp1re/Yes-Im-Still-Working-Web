package com.yesimstillworking.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UserProfileDto {
    private String username;
    private LocalDateTime accountCreated;
    private List<CommentResponseDto> historicComments;

    public UserProfileDto(String username, LocalDateTime accountCreated, List<CommentResponseDto> historicComments) {
        this.username = username;
        this.accountCreated = accountCreated;
        this.historicComments = historicComments;
    }

    // Getters
    public String getUsername() { return username; }
    public LocalDateTime getAccountCreated() { return accountCreated; }
    public List<CommentResponseDto> getHistoricComments() { return historicComments; }
}
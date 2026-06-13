package com.yesimstillworking.controller;

import com.yesimstillworking.dto.CommentResponseDto;
import com.yesimstillworking.dto.PatchResponseDto;
import com.yesimstillworking.entity.Comment;
import com.yesimstillworking.entity.Patch;
import com.yesimstillworking.entity.User;
import com.yesimstillworking.repository.CommentRepository;
import com.yesimstillworking.repository.PatchRepository;
import com.yesimstillworking.repository.UserRepository;
import com.yesimstillworking.service.PatchParserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patches")
public class PatchApiController {

    private final PatchRepository patchRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PatchParserService parserService;

    // Unified Constructor Injection
    public PatchApiController(PatchRepository patchRepository,
                              CommentRepository commentRepository,
                              UserRepository userRepository,
                              PatchParserService parserService) {
        this.patchRepository = patchRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.parserService = parserService;
    }

    // 1. SYNC FORUMS: Triggers the Jsoup deep scraper
    @GetMapping("/sync")
    public String syncPatches() {
        parserService.scrapeLatestPatches();
        return "Sync Complete";
    }

    // 2. READ ALL: Returns clean JSON mapping of all cached patches
    @GetMapping
    public List<PatchResponseDto> getAllPatches() {
        return patchRepository.findAllByOrderByReleaseDateDesc().stream()
                .map(this::convertToPatchDto)
                .collect(Collectors.toList());
    }

    // 3. ANALYSIS: Returns days passed since the last tracked game update dropped
    @GetMapping("/time-since-last")
    public Map<String, Object> getTimeSinceLastPatch() {
        Optional<Patch> latest = patchRepository.findFirstByOrderByReleaseDateDesc();
        Map<String, Object> response = new HashMap<>();

        if (latest.isPresent()) {
            long daysPassed = ChronoUnit.DAYS.between(latest.get().getReleaseDate(), LocalDate.now());
            response.put("daysSince", daysPassed);
            response.put("patchTitle", latest.get().getTitle());
        } else {
            response.put("daysSince", -1);
            response.put("message", "No database logs tracked yet. Hit /sync first.");
        }
        return response;
    }

    // 4. READ ONE: Inspect specific patch specifications by its ID
    @GetMapping("/{id}")
    public PatchResponseDto getPatchById(@PathVariable Long id) {
        Patch patch = patchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patch Not Found"));
        return convertToPatchDto(patch);
    }

    // 5. CREATE COMMENT: Authenticated profile endpoint
    @PostMapping("/{id}/comments")
    public CommentResponseDto addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        // Security Gate: Ensure user has a valid JSESSIONID cookie session
        Long loggedInId = (Long) session.getAttribute("LOGGED_IN_USER_ID");
        if (loggedInId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to leave a comment!");
        }

        Patch patch = patchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patch Not Found"));

        User user = userRepository.findById(loggedInId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        // Setup the new comment record
        Comment comment = new Comment();
        comment.setText(payload.get("text"));
        comment.setPatch(patch);
        comment.setUser(user);
        comment.setFallbackUsername(user.getUsername()); // Cache name in case account is deleted later

        Comment saved = commentRepository.save(comment);

        return new CommentResponseDto(
                saved.getId(),
                user.getId(),
                user.getUsername(),
                saved.getText(),
                saved.getCreatedAt(),
                patch.getTitle()
        );
    }

    // Data Transfer Mapping Engine: Decouples entities to clean, loop-safe JSON DTO objects
    private PatchResponseDto convertToPatchDto(Patch patch) {
        List<CommentResponseDto> commentDtos = patch.getComments().stream()
                .map(c -> {
                    // If the user profile was deleted, use our fallback snapshot name
                    String author = (c.getUser() != null) ? c.getUser().getUsername() : "[Deleted Account] (" + c.getFallbackUsername() + ")";
                    Long uid = (c.getUser() != null) ? c.getUser().getId() : null;

                    return new CommentResponseDto(
                            c.getId(),
                            uid,
                            author,
                            c.getText(),
                            c.getCreatedAt(),
                            patch.getTitle()
                    );
                })
                .collect(Collectors.toList());

        return new PatchResponseDto(
                patch.getId(),
                patch.getTitle(),
                patch.getReleaseDate(),
                patch.getDescription(),
                commentDtos
        );
    }
}
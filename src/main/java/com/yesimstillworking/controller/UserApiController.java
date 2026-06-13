package com.yesimstillworking.controller;

import com.yesimstillworking.dto.CommentResponseDto;
import com.yesimstillworking.dto.UserProfileDto;
import com.yesimstillworking.dto.UserResponseDto;
import com.yesimstillworking.entity.Comment;
import com.yesimstillworking.entity.User;
import com.yesimstillworking.repository.CommentRepository;
import com.yesimstillworking.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public UserApiController(UserRepository userRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    // 1. REGISTER ACCOUNT
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto register(@RequestBody User incomingUser) {
        if (userRepository.findByUsername(incomingUser.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken!");
        }
        User savedUser = userRepository.save(incomingUser);
        return new UserResponseDto(savedUser.getId(), savedUser.getUsername());
    }

    // 2. LOG IN: Sets browser cookies using standard context sessions
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User credentials, HttpSession session) {
        User user = userRepository.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!user.getPassword().equals(credentials.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        // Store the user ID directly into the web session wrapper
        session.setAttribute("LOGGED_IN_USER_ID", user.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully logged in as: " + user.getUsername());
        return response;
    }

    // 3. LOG OUT: Destroys the active authentication token session
    @GetMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully.");
        return response;
    }

    // 4. PUBLIC PROFILE ENQUIRY: Allows anyone to see a user's registration date and comment logs
    @GetMapping("/profile/{id}")
    public UserProfileDto getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found."));

        // Pull comment archive belonging exclusively to this profile ID
        List<CommentResponseDto> history = commentRepository.findByUserIdOrderByCreatedAtDesc(id).stream()
                .map(c -> new CommentResponseDto(
                        c.getId(),
                        user.getId(),
                        user.getUsername(),
                        c.getText(),
                        c.getCreatedAt(),
                        c.getPatch().getTitle()
                ))
                .collect(Collectors.toList());

        return new UserProfileDto(user.getUsername(), user.getCreatedAt(), history);
    }

    // 5. UPDATE USERNAME (Requires login validation validation)
    @PutMapping("/change-username")
    public UserResponseDto changeUsername(@RequestParam String newUsername, HttpSession session) {
        Long loggedInId = (Long) session.getAttribute("LOGGED_IN_USER_ID");
        if (loggedInId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to change your username!");
        }

        if (userRepository.findByUsername(newUsername).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "That username is already taken!");
        }

        User user = userRepository.findById(loggedInId).orElseThrow();
        user.setUsername(newUsername);
        userRepository.save(user);

        return new UserResponseDto(user.getId(), user.getUsername());
    }

    // 6. DELETE ACCOUNT: Safe execution that leaves historic comment artifacts intact
    @DeleteMapping("/delete-account")
    public Map<String, String> deleteAccount(HttpSession session) {
        Long loggedInId = (Long) session.getAttribute("LOGGED_IN_USER_ID");
        if (loggedInId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to delete your account.");
        }

        // Orphan Separation Block: Unlink user references so database constraints don't cascade delete comments
        List<Comment> userComments = commentRepository.findByUserIdOrderByCreatedAtDesc(loggedInId);
        for (Comment c : userComments) {
            c.setUser(null); // Sever foreign key link
            commentRepository.save(c);
        }

        // Purge user row and clean session states
        userRepository.deleteById(loggedInId);
        session.invalidate();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Your profile has been permanently closed. Comments preserved.");
        return response;
    }
}
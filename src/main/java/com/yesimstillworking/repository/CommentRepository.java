package com.yesimstillworking.repository;

import com.yesimstillworking.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPatchIdOrderByCreatedAtDesc(Long patchId);

    // New query to find comments for a user profile
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);
}
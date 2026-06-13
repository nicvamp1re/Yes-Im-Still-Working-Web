package com.yesimstillworking.repository;

import com.yesimstillworking.entity.Patch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatchRepository extends JpaRepository<Patch, Long> {

    // Used by the parser to check if we already saved this patch
    Optional<Patch> findByTitle(String title);

    // Used to show the patches on the frontend, newest first
    List<Patch> findAllByOrderByReleaseDateDesc();

    // Used to calculate the "Days since last patch" timer
    Optional<Patch> findFirstByOrderByReleaseDateDesc();
}
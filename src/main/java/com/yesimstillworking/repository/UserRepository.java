package com.yesimstillworking.repository;

import com.yesimstillworking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Crucial for looking up profiles during the login verification step
    Optional<User> findByUsername(String username);
}
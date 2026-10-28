package com.civicpilot.civicpilot.repository;

import com.civicpilot.civicpilot.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {}
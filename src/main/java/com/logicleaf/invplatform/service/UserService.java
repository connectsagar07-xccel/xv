package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.exception.BadRequestException;
import com.logicleaf.invplatform.exception.ResourceNotFoundException;
import com.logicleaf.invplatform.model.Role;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Find a user by ID
     */
    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    /**
     * Find a user by email (used in authentication & @AuthenticationPrincipal)
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users (useful for admin features)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Validate if a user exists and has a specific role
     */
    public User validateUserRole(String userId, Role expectedRole) {
        User user = findById(userId);
        if (user.getRole() != expectedRole) {
            throw new BadRequestException("User does not have required role: " + expectedRole);
        }
        return user;
    }

    /**
     * Create or update a user (optional helper)
     */
    public User saveUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        return userRepository.save(user);
    }

    /**
     * Delete user by ID (for admin or cleanup operations)
     */
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Cannot delete — user not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
}

package com.teto.planner.service;

import com.teto.planner.entity.UserEntity;
import com.teto.planner.exception.NotFoundException;
import com.teto.planner.exception.UnauthorizedException;
import com.teto.planner.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity resolve(String userIdHeader, String loginHeader) {
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                UUID id = UUID.fromString(userIdHeader);
                return userRepository.findById(id)
                        .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not found"));
            } catch (IllegalArgumentException ex) {
                throw new UnauthorizedException("UNAUTHORIZED", "Invalid X-User-Id header");
            }
        }

        if (loginHeader != null && !loginHeader.isBlank()) {
            return userRepository.findByLoginIgnoreCase(loginHeader)
                    .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not found"));
        }

        Optional<UserEntity> admin = userRepository.findByLoginIgnoreCase("admin");
        if (admin.isPresent()) {
            return admin.get();
        }

        return userRepository.findAll(PageRequest.of(0, 1)).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "No users in database"));
    }
}

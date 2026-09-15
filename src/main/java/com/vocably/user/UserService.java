package com.vocably.user;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public User createUser(String email, String displayName, String passwordHash) {
		User user = new User();
		user.setEmail(email.trim().toLowerCase());
		user.setDisplayName(displayName.trim());
		user.setPasswordHash(passwordHash);
		return userRepository.save(user);
	}

	public Optional<User> getUserById(UUID id) {
		return userRepository.findById(Objects.requireNonNull(id, "id must not be null"));
	}

	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email.trim().toLowerCase());
	}
}
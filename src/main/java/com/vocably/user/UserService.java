package com.vocably.user;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class UserService {
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User createUser(String email, String displayName, String passwordHash) {
		User user = new User();
		user.setEmail(email);
		user.setDisplayName(displayName);
		user.setPasswordHash(passwordHash);
		return userRepository.save(user);
	}

	public Optional<User> getUserById(UUID id) {
		return userRepository.findById(Objects.requireNonNull(id, "id must not be null"));
	}

	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

}

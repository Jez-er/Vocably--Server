package com.vocably.dictionary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryRepository extends JpaRepository<Dictionary, UUID> {

	/**
	 * Every lookup is scoped by owner.
	 *
	 * <p>A dictionary is private to one user, so a by-id or by-language query that ignored the
	 * owner would hand one user's data to another.
	 */
	Optional<Dictionary> findByIdAndUserId(UUID id, UUID userId);

	Optional<Dictionary> findByUserIdAndLanguageCode(UUID userId, String languageCode);

	List<Dictionary> findAllByUserId(UUID userId);

	boolean existsByUserIdAndLanguageCode(UUID userId, String languageCode);
}

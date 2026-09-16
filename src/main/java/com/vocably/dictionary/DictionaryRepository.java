package com.vocably.dictionary;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DictionaryRepository extends JpaRepository<Dictionary, UUID> {
	Optional<Dictionary> findByLanguageCode(String languageCode);
	Optional<Dictionary> findByUserIdAndLanguageCode(UUID userId, String languageCode);
	Optional<Dictionary> findByUserId(UUID userId);
}

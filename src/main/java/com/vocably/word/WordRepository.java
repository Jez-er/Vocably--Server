package com.vocably.word;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, UUID> {
	Optional<List<Word>> findByDictionaryId(String dictionaryId);
	Optional<List<Word>> findByWord(String word);
}

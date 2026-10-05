package com.vocably.word;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vocably.word.dto.WordCreateRequest;
import com.vocably.word.dto.WordResponse;

@Service
public class WordService {

	private final WordRepository wordRepository;

	public WordService(WordRepository wordRepository) {
		this.wordRepository = wordRepository;
	}

	public WordResponse createWord(WordCreateRequest request) {
		Word word = new Word();

		word.setDictionaryId(UUID.fromString(request.dictionary_id()));
		word.setWord(request.word());
		word.setDefinitions(request.definitions());
		word.setCreatedAt(LocalDateTime.now());
		word.setUpdatedAt(LocalDateTime.now());

		Word savedWord = wordRepository.save(word);

		return toResponse(savedWord);
	}

	public List<WordResponse> getAll() {
		return wordRepository.findAll()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	public WordResponse findById(UUID id) {
		Optional<Word> word = wordRepository.findById(id);

		return word
				.map(this::toResponse)
				.orElse(null);
	}

	public List<WordResponse> findByWord(String word) {
		Optional<List<Word>> words = wordRepository.findByWord(word);

		return words
				.map(list -> list.stream()
						.map(this::toResponse)
						.toList())
				.orElse(null);
	}

	public List<WordResponse> findByDictionaryId(UUID dictionaryId) {
		Optional<List<Word>> words = wordRepository.findByDictionaryId(dictionaryId);

		return words
				.map(list -> list.stream()
						.map(this::toResponse)
						.toList())
				.orElse(null);
	}

	private WordResponse toResponse(Word word) {
		return new WordResponse(
				word.getId(),
				word.getDictionaryId().toString(),
				word.getWord(),
				word.getDefinitions(),
				word.getExamples(),
				word.getSynonymsId(),
				word.getAntonymsId(),
				word.getScores(),
				word.getStatus().name(),
				word.getCreatedAt().toString(),
				word.getUpdatedAt().toString()
		);
	}
}

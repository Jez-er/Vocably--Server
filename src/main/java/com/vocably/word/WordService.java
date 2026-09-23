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

		word.setDictionary_id(UUID.fromString(request.dictionary_id()));
		word.setWord(request.word());
		word.setDefinitions(request.definitions());
		word.setCreated_at(LocalDateTime.now());
		word.setUpdated_at(LocalDateTime.now());

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

	public List<WordResponse> findByDictionaryId(String dictionaryId) {
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
				word.getDictionary_id().toString(),
				word.getWord(),
				word.getDefinitions(),
				word.getExamples(),
				word.getSynonyms_id(),
				word.getAntonyms_id(),
				word.getScores(),
				word.getStatus().name(),
				word.getCreated_at().toString(),
				word.getUpdated_at().toString()
		);
	}
}

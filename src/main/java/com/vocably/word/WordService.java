package com.vocably.word;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vocably.common.error.ResourceNotFoundException;
import com.vocably.dictionary.DictionaryService;
import com.vocably.word.dto.WordCreateRequest;
import com.vocably.word.dto.WordResponse;

/**
 * Words, always scoped to the owner of the dictionary they live in.
 *
 * <p>Every read filters by owner and every write checks the target dictionary's owner, so one
 * user's vocabulary is never visible or writable from another's token.
 */
@Service
@Transactional(readOnly = true)
public class WordService {

	private final WordRepository wordRepository;
	private final DictionaryService dictionaryService;

	public WordService(WordRepository wordRepository, DictionaryService dictionaryService) {
		this.wordRepository = wordRepository;
		this.dictionaryService = dictionaryService;
	}

	@Transactional
	public WordResponse createWord(UUID ownerId, WordCreateRequest request) {
		requireOwnedDictionary(ownerId, request.dictionaryId());

		Word word = new Word();
		word.setDictionaryId(request.dictionaryId());
		word.setWord(request.word().trim());
		word.setDefinitions(request.definitions());

		return toResponse(wordRepository.save(word));
	}

	public List<WordResponse> getAll(UUID ownerId) {
		return wordRepository.findAllOwnedBy(ownerId)
				.stream()
				.map(WordService::toResponse)
				.toList();
	}

	public WordResponse findById(UUID ownerId, UUID id) {
		return wordRepository.findOwnedById(id, ownerId)
				.map(WordService::toResponse)
				.orElseThrow(() -> ResourceNotFoundException.of("Word", id));
	}

	public List<WordResponse> findByWord(UUID ownerId, String word) {
		return wordRepository.findOwnedByWord(word, ownerId)
				.stream()
				.map(WordService::toResponse)
				.toList();
	}

	public List<WordResponse> findByDictionaryId(UUID ownerId, UUID dictionaryId) {
		requireOwnedDictionary(ownerId, dictionaryId);

		return wordRepository.findByDictionaryId(dictionaryId)
				.stream()
				.map(WordService::toResponse)
				.toList();
	}

	/** A dictionary owned by someone else is reported as missing, not as forbidden. */
	private void requireOwnedDictionary(UUID ownerId, UUID dictionaryId) {
		if (!dictionaryService.isOwnedBy(ownerId, dictionaryId)) {
			throw ResourceNotFoundException.of("Dictionary", dictionaryId);
		}
	}

	private static WordResponse toResponse(Word word) {
		return new WordResponse(
				word.getId(),
				word.getDictionaryId(),
				word.getWord(),
				word.getDefinitions(),
				word.getExamples(),
				word.getSynonymsId(),
				word.getAntonymsId(),
				word.getScores(),
				word.getStatus(),
				word.getCreatedAt(),
				word.getUpdatedAt()
		);
	}
}

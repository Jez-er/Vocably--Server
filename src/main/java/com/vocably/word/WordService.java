package com.vocably.word;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vocably.common.error.ResourceNotFoundException;
import com.vocably.dictionary.DictionaryService;
import com.vocably.word.dto.WordCreateRequest;
import com.vocably.word.dto.WordResponse;

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

    public Page<WordResponse> getAll(UUID ownerId, Pageable pageable) {
        return wordRepository.findAllOwnedBy(ownerId, pageable).map(WordService::toResponse);
    }

    public WordResponse findById(UUID ownerId, UUID id) {
        return wordRepository.findOwnedById(id, ownerId)
                .map(WordService::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("Word", id));
    }

    public Page<WordResponse> findByWord(UUID ownerId, String word, Pageable pageable) {
        return wordRepository.findOwnedByWord(word, ownerId, pageable).map(WordService::toResponse);
    }

    public Page<WordResponse> findByDictionaryId(UUID ownerId, UUID dictionaryId, Pageable pageable) {
        requireOwnedDictionary(ownerId, dictionaryId);

        return wordRepository.findByDictionaryId(dictionaryId, pageable).map(WordService::toResponse);
    }

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

package com.vocably.dictionary;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vocably.common.error.ConflictException;
import com.vocably.common.error.ResourceNotFoundException;
import com.vocably.dictionary.dto.DictionaryCreateRequest;
import com.vocably.dictionary.dto.DictionaryResponse;
import com.vocably.language.LanguageService;

/**
 * Dictionaries, always scoped to their owner.
 *
 * <p>Every method takes the owner's id and filters on it, so a caller cannot reach a dictionary
 * belonging to another user — a missing row and someone else's row are both reported as 404.
 */
@Service
@Transactional(readOnly = true)
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final LanguageService languageService;

    public DictionaryService(
            DictionaryRepository dictionaryRepository,
            LanguageService languageService
    ) {
        this.dictionaryRepository = dictionaryRepository;
        this.languageService = languageService;
    }

    @Transactional
    public DictionaryResponse createDictionary(UUID ownerId, DictionaryCreateRequest request) {
        String languageCode = request.languageCode().trim().toLowerCase();

        // Checked up front so an unknown code is a 404 about the language, rather than a foreign
        // key violation surfacing as a generic conflict.
        if (!languageService.existsByCode(languageCode)) {
            throw ResourceNotFoundException.of("Language", languageCode);
        }

        if (dictionaryRepository.existsByUserIdAndLanguageCode(ownerId, languageCode)) {
            throw new ConflictException("A dictionary for language '" + languageCode + "' already exists");
        }

        Dictionary dictionary = new Dictionary();
        dictionary.setUserId(ownerId);
        dictionary.setLanguageCode(languageCode);

        return toResponse(dictionaryRepository.save(dictionary));
    }

    public DictionaryResponse getDictionaryById(UUID ownerId, UUID id) {
        return dictionaryRepository.findByIdAndUserId(id, ownerId)
                .map(DictionaryService::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("Dictionary", id));
    }

    public DictionaryResponse getDictionaryByLanguageCode(UUID ownerId, String code) {
        String languageCode = code.trim().toLowerCase();

        return dictionaryRepository.findByUserIdAndLanguageCode(ownerId, languageCode)
                .map(DictionaryService::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.of("Dictionary for language", languageCode));
    }

    public List<DictionaryResponse> getAllDictionaries(UUID ownerId) {
        return dictionaryRepository.findAllByUserId(ownerId)
                .stream()
                .map(DictionaryService::toResponse)
                .toList();
    }

    /** Whether the dictionary exists and belongs to the given user. */
    public boolean isOwnedBy(UUID ownerId, UUID dictionaryId) {
        return dictionaryRepository.findByIdAndUserId(dictionaryId, ownerId).isPresent();
    }

    private static DictionaryResponse toResponse(Dictionary dictionary) {
        return new DictionaryResponse(
                dictionary.getId(),
                dictionary.getUserId(),
                dictionary.getLanguageCode()
        );
    }
}

package com.vocably.dictionary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vocably.dictionary.dto.DictionaryCreateRequest;
import com.vocably.dictionary.dto.DictionaryResponse;

@Service
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;

    public DictionaryService(
            DictionaryRepository dictionaryRepository
    ) {
        this.dictionaryRepository = dictionaryRepository;
    }

    public DictionaryResponse createDictionary(DictionaryCreateRequest request) {

        Dictionary dictionary = new Dictionary();

        dictionary.setUser_id(UUID.fromString(request.UserId()));
        dictionary.setLanguage_code(request.LanguageCode());

        Dictionary savedDictionary = dictionaryRepository.save(dictionary);

        return new DictionaryResponse(
                savedDictionary.getId(),
                savedDictionary.getUser_id().toString(),
                savedDictionary.getLanguage_code()
        );
    }

    public DictionaryResponse getDictionaryByLanguageCode(String code) {

        Optional<Dictionary> dictionary =
                dictionaryRepository.findByLanguageCode(code);

        return dictionary
                .map(dict -> new DictionaryResponse(
                        dict.getId(),
                        dict.getUser_id().toString(),
                        dict.getLanguage_code()
                ))
                .orElse(null);
    }

    public DictionaryResponse getDictionaryById(String id) {

        Optional<Dictionary> dictionary =
                dictionaryRepository.findById(UUID.fromString(id));

        return dictionary	
                .map(dict -> new DictionaryResponse(
                        dict.getId(),
                        dict.getUser_id().toString(),
                        dict.getLanguage_code()
                ))
                .orElse(null);
    }

    public List<DictionaryResponse> getAllLanguages() {

        return dictionaryRepository.findAll()
                .stream()
                .map(lang -> new DictionaryResponse(
                        lang.getId(),
                        lang.getUser_id().toString(),
                        lang.getLanguage_code()
                ))
                .toList();
    }

}
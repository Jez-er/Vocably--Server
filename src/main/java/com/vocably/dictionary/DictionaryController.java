package com.vocably.dictionary;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.dictionary.dto.DictionaryCreateRequest;
import com.vocably.dictionary.dto.DictionaryResponse;

@RestController
@RequestMapping("/api/dictionaries")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping
    public ResponseEntity<DictionaryResponse> createDictionary(
            @RequestBody DictionaryCreateRequest request
    ) {
        DictionaryResponse response =
                dictionaryService.createDictionary(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DictionaryResponse> getDictionaryById(
            @PathVariable String id
    ) {
        DictionaryResponse response =
                dictionaryService.getDictionaryById(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/language/{code}")
    public ResponseEntity<DictionaryResponse> getDictionaryByLanguageCode(
            @PathVariable String code
    ) {
        DictionaryResponse response =
                dictionaryService.getDictionaryByLanguageCode(code);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DictionaryResponse>> getAllDictionaries() {

        List<DictionaryResponse> response =
                dictionaryService.getAllLanguages();

        return ResponseEntity.ok(response);
    }
}
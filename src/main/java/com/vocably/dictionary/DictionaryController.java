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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dictionaries")
@Tag(name = "Dictionaries", description = "Dictionary management")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new dictionary",
            description = "Creates a new dictionary from the provided request body and returns the created resource."
    )
    @ApiResponse(responseCode = "201", description = "Dictionary created successfully")
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
    @Operation(
            summary = "Get a dictionary by ID",
            description = "Retrieves a single dictionary by its unique identifier."
    )
    @ApiResponse(responseCode = "200", description = "Dictionary found")
    @ApiResponse(responseCode = "404", description = "Dictionary not found")
    public ResponseEntity<DictionaryResponse> getDictionaryById(
            @Parameter(description = "Unique identifier of the dictionary") @PathVariable String id
    ) {
        DictionaryResponse response =
                dictionaryService.getDictionaryById(id);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/language/{code}")
    @Operation(
            summary = "Get a dictionary by language code",
            description = "Retrieves a single dictionary matching the given language code."
    )
    @ApiResponse(responseCode = "200", description = "Dictionary found")
    @ApiResponse(responseCode = "404", description = "Dictionary not found for the given language code")
    public ResponseEntity<DictionaryResponse> getDictionaryByLanguageCode(
            @Parameter(description = "Language code of the dictionary") @PathVariable String code
    ) {
        DictionaryResponse response =
                dictionaryService.getDictionaryByLanguageCode(code);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Get all dictionaries",
            description = "Retrieves a list of all available dictionaries."
    )
    @ApiResponse(responseCode = "200", description = "List of dictionaries retrieved successfully")
    public ResponseEntity<List<DictionaryResponse>> getAllDictionaries() {

        List<DictionaryResponse> response =
                dictionaryService.getAllLanguages();

        return ResponseEntity.ok(response);
    }
}
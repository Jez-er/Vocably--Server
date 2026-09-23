package com.vocably.word;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.word.dto.WordCreateRequest;
import com.vocably.word.dto.WordResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/words")
@Tag(name = "Words", description = "Word management")
public class WordController {

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @PostMapping
    @Operation(summary = "Create a word", description = "Creates a new word entry")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Word created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<WordResponse> createWord(@RequestBody WordCreateRequest request) {
        WordResponse response = wordService.createWord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get word by ID", description = "Retrieves a word by its unique identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Word found"),
        @ApiResponse(responseCode = "404", description = "Word not found")
    })
    public ResponseEntity<WordResponse> getWordById(
            @Parameter(description = "UUID of the word") @PathVariable String id) {
        WordResponse response = wordService.findById(UUID.fromString(id));
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/{word}")
    @Operation(summary = "Search words by text", description = "Searches for words matching the given text")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Words found"),
        @ApiResponse(responseCode = "404", description = "No words found")
    })
    public ResponseEntity<List<WordResponse>> searchByWord(
            @Parameter(description = "Word text to search for") @PathVariable String word) {
        List<WordResponse> responses = wordService.findByWord(word);
        if (responses == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/dictionary/{dictionaryId}")
    @Operation(summary = "Get words by dictionary", description = "Retrieves all words belonging to a specific dictionary")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Words found"),
        @ApiResponse(responseCode = "404", description = "No words found for the given dictionary")
    })
    public ResponseEntity<List<WordResponse>> getWordsByDictionary(
            @Parameter(description = "UUID of the dictionary") @PathVariable String dictionaryId) {
        List<WordResponse> responses = wordService.findByDictionaryId(dictionaryId);
        if (responses == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    @Operation(summary = "Get all words", description = "Retrieves all available words")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Words retrieved successfully")
    })
    public ResponseEntity<List<WordResponse>> getAllWords() {
        List<WordResponse> responses = wordService.getAll();
        return ResponseEntity.ok(responses);
    }
}

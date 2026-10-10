package com.vocably.word;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.auth.UserPrincipal;
import com.vocably.common.error.ApiErrorResponse;
import com.vocably.word.dto.WordCreateRequest;
import com.vocably.word.dto.WordResponse;

import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/words")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Words", description = "Word management, scoped to the authenticated user")
public class WordController {

    private static final String CREATED_AT = "createdAt";

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a word", description = "Adds a word to one of the authenticated user's dictionaries.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Word created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed (code VALIDATION_FAILED, with fieldErrors)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "The dictionary does not exist or is not yours (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public WordResponse createWord(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WordCreateRequest request
    ) {
        return wordService.createWord(principal.getId(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get word by ID", description = "Retrieves one of the authenticated user's words by its identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Word found"),
        @ApiResponse(responseCode = "404", description = "Word not found, or not yours (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public WordResponse getWordById(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID of the word") @PathVariable UUID id
    ) {
        return wordService.findById(principal.getId(), id);
    }

    @GetMapping("/search/{word}")
    @Operation(
            summary = "Search your words by text",
            description = "Case-insensitive exact match over the authenticated user's words. Paged; returns an empty page when nothing matches."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matching words, possibly none")
    })
    public PagedModel<WordResponse> searchByWord(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Word text to search for") @PathVariable String word,
            @ParameterObject @PageableDefault(sort = CREATED_AT, direction = Direction.DESC) Pageable pageable
    ) {
        return new PagedModel<>(wordService.findByWord(principal.getId(), word, pageable));
    }

    @GetMapping("/dictionary/{dictionaryId}")
    @Operation(
            summary = "Get words by dictionary",
            description = "Retrieves the words in one of the authenticated user's dictionaries. Paged; returns an empty page when the dictionary has none."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Words in the dictionary, possibly none"),
        @ApiResponse(responseCode = "404", description = "The dictionary does not exist or is not yours (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public PagedModel<WordResponse> getWordsByDictionary(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "UUID of the dictionary") @PathVariable UUID dictionaryId,
            @ParameterObject @PageableDefault(sort = CREATED_AT, direction = Direction.DESC) Pageable pageable
    ) {
        return new PagedModel<>(wordService.findByDictionaryId(principal.getId(), dictionaryId, pageable));
    }

    @GetMapping
    @Operation(
            summary = "Get all your words",
            description = "Retrieves every word across the authenticated user's dictionaries, newest first. Paged."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Words retrieved successfully")
    })
    public PagedModel<WordResponse> getAllWords(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @ParameterObject @PageableDefault(sort = CREATED_AT, direction = Direction.DESC) Pageable pageable
    ) {
        return new PagedModel<>(wordService.getAll(principal.getId(), pageable));
    }
}

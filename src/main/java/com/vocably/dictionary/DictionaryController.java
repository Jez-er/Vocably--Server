package com.vocably.dictionary;

import java.util.List;
import java.util.UUID;

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
import com.vocably.dictionary.dto.DictionaryCreateRequest;
import com.vocably.dictionary.dto.DictionaryResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dictionaries")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Dictionaries", description = "Dictionary management, scoped to the authenticated user")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new dictionary",
            description = "Creates a dictionary owned by the authenticated user. The owner is taken from the access token, not from the request body."
    )
    @ApiResponse(responseCode = "201", description = "Dictionary created successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed (code VALIDATION_FAILED, with fieldErrors)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Unknown language code (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "The user already has a dictionary for that language (code CONFLICT)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public DictionaryResponse createDictionary(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DictionaryCreateRequest request
    ) {
        return dictionaryService.createDictionary(principal.getId(), request);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get one of your dictionaries by ID",
            description = "Retrieves a dictionary owned by the authenticated user. A dictionary owned by someone else is reported as not found."
    )
    @ApiResponse(responseCode = "200", description = "Dictionary found")
    @ApiResponse(responseCode = "404", description = "Dictionary not found (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public DictionaryResponse getDictionaryById(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Unique identifier of the dictionary") @PathVariable UUID id
    ) {
        return dictionaryService.getDictionaryById(principal.getId(), id);
    }

    @GetMapping("/language/{code}")
    @Operation(
            summary = "Get your dictionary for a language",
            description = "Retrieves the authenticated user's dictionary for the given language code."
    )
    @ApiResponse(responseCode = "200", description = "Dictionary found")
    @ApiResponse(responseCode = "404", description = "No dictionary for that language code (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public DictionaryResponse getDictionaryByLanguageCode(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Language code of the dictionary") @PathVariable String code
    ) {
        return dictionaryService.getDictionaryByLanguageCode(principal.getId(), code);
    }

    @GetMapping
    @Operation(
            summary = "Get all your dictionaries",
            description = "Retrieves every dictionary owned by the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "List of dictionaries retrieved successfully")
    public List<DictionaryResponse> getAllDictionaries(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal
    ) {
        return dictionaryService.getAllDictionaries(principal.getId());
    }
}

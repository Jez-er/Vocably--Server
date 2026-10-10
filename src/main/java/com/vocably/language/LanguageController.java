package com.vocably.language;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.common.error.ApiErrorResponse;
import com.vocably.language.dto.LanguageCreateRequest;
import com.vocably.language.dto.LanguageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/languages")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Languages", description = "Language management")
public class LanguageController {
    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new language",
        description = "Creates a new language entry. Admin only — the catalogue is shared reference data. Title, code, and flag are required fields."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Language created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed (code VALIDATION_FAILED, with fieldErrors)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid access token (code UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Authenticated but not an admin (code FORBIDDEN)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Language with the given code already exists (code LANGUAGE_ALREADY_EXISTS)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public LanguageResponse createLanguage(@Valid @RequestBody LanguageCreateRequest request) {
        return languageService.createLanguage(request);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get language by code", description = "Retrieves a language by its unique code.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Language found and returned"),
        @ApiResponse(responseCode = "404", description = "Language not found for the given code (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public LanguageResponse getLanguageByCode(
            @Parameter(description = "The unique code of the language") @PathVariable String code) {
        return languageService.getLanguageByCode(code);
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Get language by ID", description = "Retrieves a language by its unique identifier.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Language found and returned"),
        @ApiResponse(responseCode = "404", description = "Language not found for the given ID (code NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public LanguageResponse getLanguageById(
            @Parameter(description = "The unique identifier of the language") @PathVariable UUID id) {
        return languageService.getLanguageById(id);
    }

    @GetMapping({"", "/all"})
    @Operation(summary = "Get all languages", description = "Retrieves all available languages.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of all languages returned")
    })
    public List<LanguageResponse> getAllLanguages() {
        return languageService.getAllLanguages();
    }
}

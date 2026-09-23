package com.vocably.language;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.language.dto.LanguageCreateRequest;
import com.vocably.language.dto.LanguageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/languages")
@Tag(name = "Languages", description = "Language management")
public class LanguageController {
	private final LanguageService languageService;

	public LanguageController(LanguageService languageService) {
		this.languageService = languageService;
	}
	
	@PostMapping()
	@Operation(summary = "Create a new language", description = "Creates a new language entry. Title, code, and flag are required fields.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Language created successfully"),
		@ApiResponse(responseCode = "200", description = "Validation error or conflict message returned"),
		@ApiResponse(responseCode = "409", description = "Language with the given code already exists")
	})
	public String createLanguage(LanguageCreateRequest request) {
		if (request.title() == null || request.title().isBlank()) {
			return "Title is required";
		}
		if (request.code() == null || request.code().isBlank()) {
			return "Code is required";
		}
		if (request.flag() == null || request.flag().isBlank()) {
			return "Flag is required";
		}

		if (languageService.getLanguageByCode(request.code()) != null) {
			return "Language with this code already exists";
		}

		languageService.createLanguage(request);
		return "Language created successfully";
	}

	@GetMapping("/code/{code}")
	@Operation(summary = "Get language by code", description = "Retrieves a language by its unique code.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Language found and returned"),
		@ApiResponse(responseCode = "404", description = "Language not found for the given code")
	})
	public LanguageResponse getLanguageByCode(
			@Parameter(description = "The unique code of the language") @PathVariable String code) {
		return languageService.getLanguageByCode(code);
	}

	@GetMapping("/id/{id}")
	@Operation(summary = "Get language by ID", description = "Retrieves a language by its unique identifier.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Language found and returned"),
		@ApiResponse(responseCode = "404", description = "Language not found for the given ID")
	})
	public LanguageResponse getLanguageById(
			@Parameter(description = "The unique identifier of the language") @PathVariable String id) {
		return languageService.getLanguageById(id);
	}

	@GetMapping("/all")
	@Operation(summary = "Get all languages", description = "Retrieves all available languages.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "List of all languages returned")
	})
	public LanguageResponse getLanguageAll() {
		return languageService.getLanguageAll();
	}
}

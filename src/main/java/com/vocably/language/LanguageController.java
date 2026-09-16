package com.vocably.language;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vocably.language.dto.LanguageCreateRequest;
import com.vocably.language.dto.LanguageResponse;

@RestController
@RequestMapping("/api/languages")
public class LanguageController {
	private final LanguageService languageService;

	public LanguageController(LanguageService languageService) {
		this.languageService = languageService;
	}
	
	@PostMapping()
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
	public LanguageResponse getLanguageByCode(@PathVariable String code) {
		return languageService.getLanguageByCode(code);
	}

	@GetMapping("/id/{id}")
	public LanguageResponse getLanguageById(@PathVariable String id) {
		return languageService.getLanguageById(id);
	}

	@GetMapping("/all")
	public LanguageResponse getLanguageAll() {
		return languageService.getLanguageAll();
	}
}

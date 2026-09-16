package com.vocably.language;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vocably.language.dto.LanguageCreateRequest;
import com.vocably.language.dto.LanguageResponse;

@Service 
public class LanguageService {
	private final LanguageRepository languageRepository;

	public LanguageService(LanguageRepository languageRepository) {
		this.languageRepository = languageRepository;
	}


	public Language createLanguage(LanguageCreateRequest request) {
		Language language = new Language();
		language.setTitle(request.title().trim());
		language.setCode(request.code().trim().toLowerCase());
		language.setFlag(request.flag().trim());
		return language;
	}

	public LanguageResponse getLanguageByCode(String code) {
		Optional<Language> language = languageRepository.findByCode(code);
		return language.map(lang -> new LanguageResponse(lang.getId(), lang.getTitle(), lang.getCode(), lang.getFlag()))
				.orElse(null);
	}

	public LanguageResponse getLanguageById(String id) {
		Optional<Language> language = languageRepository.findById(java.util.UUID.fromString(id));
		return language.map(lang -> new LanguageResponse(lang.getId(), lang.getTitle(), lang.getCode(), lang.getFlag()))
				.orElse(null);
	}

	public LanguageResponse getLanguageAll() {
		Optional<Language> language = languageRepository.findAll().stream().findFirst();
		return language.map(lang -> new LanguageResponse(lang.getId(), lang.getTitle(), lang.getCode(), lang.getFlag()))
				.orElse(null);
	}
}

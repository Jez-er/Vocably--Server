package com.vocably.language;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vocably.common.error.ConflictException;
import com.vocably.common.error.ErrorCode;
import com.vocably.common.error.ResourceNotFoundException;
import com.vocably.language.dto.LanguageCreateRequest;
import com.vocably.language.dto.LanguageResponse;

@Service
@Transactional(readOnly = true)
public class LanguageService {
	private final LanguageRepository languageRepository;

	public LanguageService(LanguageRepository languageRepository) {
		this.languageRepository = languageRepository;
	}

	@Transactional
	public LanguageResponse createLanguage(LanguageCreateRequest request) {
		String code = request.code().trim().toLowerCase();

		if (languageRepository.existsByCode(code)) {
			throw new ConflictException(
					ErrorCode.LANGUAGE_ALREADY_EXISTS,
					"Language with code '" + code + "' already exists"
			);
		}

		Language language = new Language();
		language.setTitle(request.title().trim());
		language.setCode(code);
		language.setFlag(request.flag().trim());

		return toResponse(languageRepository.save(language));
	}

	public LanguageResponse getLanguageByCode(String code) {
		return languageRepository.findByCode(code.trim().toLowerCase())
				.map(LanguageService::toResponse)
				.orElseThrow(() -> ResourceNotFoundException.of("Language", code));
	}

	public LanguageResponse getLanguageById(UUID id) {
		return languageRepository.findById(id)
				.map(LanguageService::toResponse)
				.orElseThrow(() -> ResourceNotFoundException.of("Language", id));
	}

	public List<LanguageResponse> getAllLanguages() {
		return languageRepository.findAll()
				.stream()
				.map(LanguageService::toResponse)
				.toList();
	}

	public boolean existsByCode(String code) {
		return languageRepository.existsByCode(code.trim().toLowerCase());
	}

	private static LanguageResponse toResponse(Language language) {
		return new LanguageResponse(
				language.getId(),
				language.getTitle(),
				language.getCode(),
				language.getFlag()
		);
	}
}

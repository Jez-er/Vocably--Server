package com.vocably.language.dto;

import java.util.UUID;


public record LanguageResponse(
    UUID id,
		String title,
		String code,
		String flag
) {}
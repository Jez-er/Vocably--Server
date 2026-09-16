package com.vocably.dictionary.dto;

import java.util.UUID;

public record DictionaryResponse(
    UUID id,
		String userId,
		String LanguageCode
) {}

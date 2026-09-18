package com.vocably.word.dto;

import java.util.UUID;

public record WordResponse(
    UUID id,
		String dictionary_id,
		String word,
		String[] definitions,
		String[] examples,
		String[] synonyms_id,
		String[] antonyms_id,
		Integer Scores,
		String status,
		String created_at,
		String updated_at
) {}

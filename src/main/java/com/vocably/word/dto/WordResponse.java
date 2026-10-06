package com.vocably.word.dto;

import java.time.Instant;
import java.util.UUID;

import com.vocably.word.enums.WordStatus;

public record WordResponse(
    UUID id,
    UUID dictionaryId,
    String word,
    String[] definitions,
    String[] examples,
    String[] synonymsId,
    String[] antonymsId,
    Integer scores,
    WordStatus status,
    Instant createdAt,
    Instant updatedAt
) {}

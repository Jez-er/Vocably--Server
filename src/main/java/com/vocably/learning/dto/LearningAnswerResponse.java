package com.vocably.learning.dto;

import java.time.Instant;
import java.util.UUID;

import com.vocably.word.enums.WordStatus;

public record LearningAnswerResponse(
    UUID wordId,
    Integer scores,
    WordStatus status,
    WordStatus previousStatus,
    Instant lastLearnedAt
) {}

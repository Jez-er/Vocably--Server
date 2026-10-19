package com.vocably.learning.dto;

import java.util.List;

import com.vocably.word.dto.WordResponse;

public record LearningPoolResponse(
    int dailyLimit,
    int completedToday,
    int remaining,
    List<WordResponse> words
) {}

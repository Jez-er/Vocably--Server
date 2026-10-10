package com.vocably.word.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record WordCreateRequest(

    @NotNull
    UUID dictionaryId,

    @NotBlank
    String word,

    @NotEmpty
    String[] definitions

) {}

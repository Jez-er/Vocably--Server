package com.vocably.dictionary.dto;

import jakarta.validation.constraints.NotBlank;

public record DictionaryCreateRequest(

    @NotBlank 
    String UserId,

    @NotBlank
    String LanguageCode

) {}
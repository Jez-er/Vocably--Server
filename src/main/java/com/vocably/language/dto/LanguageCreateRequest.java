package com.vocably.language.dto;

import jakarta.validation.constraints.NotBlank;

public record LanguageCreateRequest(

    @NotBlank 
    String title,

    @NotBlank
    String code,

    @NotBlank
    String flag

) {}
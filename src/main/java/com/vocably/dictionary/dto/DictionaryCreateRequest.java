package com.vocably.dictionary.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * A request to create a dictionary for the caller.
 *
 * <p>There is deliberately no user id here. It used to be taken from the body, which let any
 * authenticated user create a dictionary owned by someone else; the owner is now read from the
 * access token instead.
 */
public record DictionaryCreateRequest(

    @NotBlank
    String languageCode

) {}

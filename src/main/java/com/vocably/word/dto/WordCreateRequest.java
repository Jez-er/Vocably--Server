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

	// @NotEmpty, not @NotBlank: @NotBlank only applies to CharSequence, and on an array it throws
	// UnexpectedTypeException as soon as validation actually runs.
	@NotEmpty
	String[] definitions

) {}

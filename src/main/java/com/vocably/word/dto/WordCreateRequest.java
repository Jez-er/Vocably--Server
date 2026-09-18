package com.vocably.word.dto;

import jakarta.validation.constraints.NotBlank;

public record WordCreateRequest(

	@NotBlank 
	String dictionary_id,

  @NotBlank
  String word,

  @NotBlank
  String[] definitions

) {}

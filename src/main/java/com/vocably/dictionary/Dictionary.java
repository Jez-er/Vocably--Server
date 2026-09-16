package com.vocably.dictionary;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "dictionaries")
@Getter 
@Setter 
@NoArgsConstructor 
public class Dictionary {
	@Id 
  @GeneratedValue 
  private UUID id;

	@Column (nullable = false)
	private UUID user_id;

  @Column (nullable = false)
  private String language_code;
}
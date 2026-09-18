package com.vocably.word;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vocably.word.enums.WordStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;




@Entity 
@Table (name = "words")
@Getter 
@Setter 
@NoArgsConstructor 
public class Word {
	@Id 
  @GeneratedValue 
  private UUID id;

	@Column (nullable = false)
	private UUID dictionary_id;

  @Column (nullable = false)
  private String word;

  @Column (nullable = false)
  private String[] definitions;

  @Column (nullable = true)
  private String[] examples;

  @Column (nullable = true)
  private String[] synonyms_id;

  @Column (nullable = true)
  private String[] antonyms_id;

  @Column (nullable = false)
  private Integer Scores = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private WordStatus status = WordStatus.SEED; 

  @Column(name = "created_at", nullable = false)
  private LocalDateTime created_at;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updated_at;
}
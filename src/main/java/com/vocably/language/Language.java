package com.vocably.language;

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
@Table (name = "languages")
@Getter 
@Setter 
@NoArgsConstructor 
public class Language {
	@Id 
  @GeneratedValue
  private UUID id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, unique = true)
  private String code;

	@Column (nullable = false)
	private String flag;
}

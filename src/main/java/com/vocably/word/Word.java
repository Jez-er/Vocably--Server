package com.vocably.word;

import java.time.Instant;
import java.util.UUID;

import com.vocably.word.enums.WordStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "words")
@Getter
@Setter
@NoArgsConstructor
public class Word {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, name = "dictionary_id")
    private UUID dictionaryId;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String[] definitions;

    @Column(nullable = true)
    private String[] examples;

    @Column(nullable = true, name = "synonyms_id")
    private String[] synonymsId;

    @Column(nullable = true, name = "antonyms_id")
    private String[] antonymsId;

    @Column(name = "scores", nullable = false)
    private Integer scores = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WordStatus status = WordStatus.SEED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}

package com.vocably.word;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WordRepository extends JpaRepository<Word, UUID> {

    @Query("""
            select w from Word w
            where w.dictionaryId in (select d.id from Dictionary d where d.userId = :userId)
            """)
    Page<Word> findAllOwnedBy(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            select w from Word w
            where w.id = :id
              and w.dictionaryId in (select d.id from Dictionary d where d.userId = :userId)
            """)
    Optional<Word> findOwnedById(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("""
            select w from Word w
            where lower(w.word) = lower(:word)
              and w.dictionaryId in (select d.id from Dictionary d where d.userId = :userId)
            """)
    Page<Word> findOwnedByWord(@Param("word") String word, @Param("userId") UUID userId, Pageable pageable);

    Page<Word> findByDictionaryId(UUID dictionaryId, Pageable pageable);
}

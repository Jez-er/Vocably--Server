package com.vocably.word;

import java.time.Instant;
import java.util.List;
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

    @Query(value = """
            select w.id from words w
            where w.dictionary_id = :dictionaryId
              and w.status = :status
              and (
                    w.last_learned_at is null
                 or w.last_learned_at >= cast(:todayStart as timestamptz)
                 or w.last_learned_at <= cast(:now as timestamptz) - interval '1 day' * (
                        :minDays + mod(
                            ('x' || substr(md5(w.id::text || coalesce(w.last_learned_at::text, '')), 1, 7))::bit(28)::int,
                            :spreadDays
                        )
                    )
              )
            order by md5(w.id::text || :daySeed)
            limit :maxCandidates
            """, nativeQuery = true)
    List<UUID> findDailyCandidateIds(
            @Param("dictionaryId") UUID dictionaryId,
            @Param("status") String status,
            @Param("now") Instant now,
            @Param("todayStart") Instant todayStart,
            @Param("minDays") int minDays,
            @Param("spreadDays") int spreadDays,
            @Param("daySeed") String daySeed,
            @Param("maxCandidates") int maxCandidates
    );
}

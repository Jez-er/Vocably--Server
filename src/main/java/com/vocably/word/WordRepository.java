package com.vocably.word;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WordRepository extends JpaRepository<Word, UUID> {

	/**
	 * Owner-scoped lookups.
	 *
	 * <p>A word has no direct user column, so ownership is resolved through the dictionary it
	 * belongs to. Written as explicit queries rather than derived methods because the filter is a
	 * subquery across entities.
	 *
	 * <p>These return plain {@code List}, not {@code Optional<List>}: a derived collection query is
	 * never empty-Optional, so the old signature promised an absence that could not happen and made
	 * "no rows" look like "not found".
	 */
	@Query("""
			select w from Word w
			where w.dictionaryId in (select d.id from Dictionary d where d.userId = :userId)
			""")
	List<Word> findAllOwnedBy(@Param("userId") UUID userId);

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
	List<Word> findOwnedByWord(@Param("word") String word, @Param("userId") UUID userId);

	List<Word> findByDictionaryId(UUID dictionaryId);
}

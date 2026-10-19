package com.vocably.learning;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vocably.common.error.ResourceNotFoundException;
import com.vocably.dictionary.DictionaryService;
import com.vocably.learning.dto.LearningAnswerResponse;
import com.vocably.learning.dto.LearningPoolResponse;
import com.vocably.word.Word;
import com.vocably.word.WordRepository;
import com.vocably.word.WordService;
import com.vocably.word.enums.WordStatus;

@Service
@Transactional(readOnly = true)
public class LearningService {

    private static final int DAILY_LIMIT = 25;

    private static final int SPROUT_PROMOTION_SCORE = 100;

    private static final int BLOOM_PROMOTION_SCORE = 150;

    private static final List<Tier> TIERS = List.of(
            new Tier(WordStatus.SEED, 13, 1, 3),
            new Tier(WordStatus.SPROUT, 8, 7, 8),
            new Tier(WordStatus.BLOOM, 4, 30, 1)
    );

    private final WordRepository wordRepository;
    private final DictionaryService dictionaryService;

    public LearningService(WordRepository wordRepository, DictionaryService dictionaryService) {
        this.wordRepository = wordRepository;
        this.dictionaryService = dictionaryService;
    }

    public LearningPoolResponse getDailyPool(UUID ownerId, UUID dictionaryId) {
        requireOwnedDictionary(ownerId, dictionaryId);

        Instant now = Instant.now();
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        Instant todayStart = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        String daySeed = ownerId + ":" + dictionaryId + ":" + today;

        List<List<UUID>> candidates = TIERS.stream()
                .map(tier -> wordRepository.findDailyCandidateIds(
                        dictionaryId,
                        tier.status().name(),
                        now,
                        todayStart,
                        tier.minDays(),
                        tier.spreadDays(),
                        daySeed,
                        DAILY_LIMIT
                ))
                .toList();

        List<Word> pool = wordRepository.findAllById(allocate(candidates));

        List<Word> remaining = new ArrayList<>(pool.size());

        for (Word word : pool) {
            if (word.getLastLearnedAt() == null || word.getLastLearnedAt().isBefore(todayStart)) {
                remaining.add(word);
            }
        }

        Collections.shuffle(remaining, new Random(daySeed.hashCode()));

        return new LearningPoolResponse(
                DAILY_LIMIT,
                pool.size() - remaining.size(),
                remaining.size(),
                remaining.stream().map(WordService::toResponse).toList()
        );
    }

    @Transactional
    public LearningAnswerResponse submitResult(UUID ownerId, UUID wordId, boolean successful) {
        Word word = wordRepository.findOwnedById(wordId, ownerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Word", wordId));

        WordStatus previousStatus = word.getStatus();
        int scores = successful ? word.getScores() + 1 : Math.max(0, word.getScores() - 1);

        word.setScores(scores);
        word.setStatus(statusFor(scores));

        if (successful) {
            word.setLastLearnedAt(Instant.now());
        }

        Word saved = wordRepository.save(word);

        return new LearningAnswerResponse(
                saved.getId(),
                saved.getScores(),
                saved.getStatus(),
                previousStatus,
                saved.getLastLearnedAt()
        );
    }

    private void requireOwnedDictionary(UUID ownerId, UUID dictionaryId) {
        if (!dictionaryService.isOwnedBy(ownerId, dictionaryId)) {
            throw ResourceNotFoundException.of("Dictionary", dictionaryId);
        }
    }

    private static List<UUID> allocate(List<List<UUID>> candidates) {
        List<UUID> selected = new ArrayList<>(DAILY_LIMIT);
        int[] taken = new int[candidates.size()];

        for (int tier = 0; tier < candidates.size(); tier++) {
            taken[tier] = Math.min(TIERS.get(tier).quota(), candidates.get(tier).size());
            selected.addAll(candidates.get(tier).subList(0, taken[tier]));
        }

        for (int tier = 0; tier < candidates.size() && selected.size() < DAILY_LIMIT; tier++) {
            List<UUID> tierCandidates = candidates.get(tier);
            int extra = Math.min(tierCandidates.size() - taken[tier], DAILY_LIMIT - selected.size());

            selected.addAll(tierCandidates.subList(taken[tier], taken[tier] + extra));
        }

        return selected;
    }

    private static WordStatus statusFor(int scores) {
        if (scores >= BLOOM_PROMOTION_SCORE) {
            return WordStatus.BLOOM;
        }

        if (scores >= SPROUT_PROMOTION_SCORE) {
            return WordStatus.SPROUT;
        }

        return WordStatus.SEED;
    }

    private record Tier(WordStatus status, int quota, int minDays, int spreadDays) {}
}

package com.vocably.learning;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vocably.common.error.ResourceNotFoundException;
import com.vocably.dictionary.DictionaryService;
import com.vocably.dictionary.dto.DictionaryCreateRequest;
import com.vocably.learning.dto.LearningAnswerResponse;
import com.vocably.learning.dto.LearningPoolResponse;
import com.vocably.support.PostgresIntegrationTest;
import com.vocably.user.User;
import com.vocably.user.UserService;
import com.vocably.word.Word;
import com.vocably.word.WordRepository;
import com.vocably.word.dto.WordResponse;
import com.vocably.word.enums.WordStatus;

class LearningIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private LearningService learningService;

    @Autowired
    private WordRepository wordRepository;

    private UUID alice;
    private UUID aliceDictionary;

    @BeforeEach
    void createDictionary() {
        alice = newUser("alice").getId();
        aliceDictionary = dictionaryService
                .createDictionary(alice, new DictionaryCreateRequest("en"))
                .id();
    }

    @Test
    void pool_isCappedAtTwentyFiveWords() {
        seedWords(aliceDictionary, WordStatus.SEED, 40, null);

        LearningPoolResponse pool = learningService.getDailyPool(alice, aliceDictionary);

        assertThat(pool.words()).hasSize(25);
        assertThat(pool.dailyLimit()).isEqualTo(25);
        assertThat(pool.remaining()).isEqualTo(25);
        assertThat(pool.completedToday()).isZero();
    }

    @Test
    void pool_prefersSeedThenSproutThenBloom() {
        seedWords(aliceDictionary, WordStatus.SEED, 40, null);
        seedWords(aliceDictionary, WordStatus.SPROUT, 40, null);
        seedWords(aliceDictionary, WordStatus.BLOOM, 40, null);

        List<WordResponse> words = learningService.getDailyPool(alice, aliceDictionary).words();

        assertThat(words).hasSize(25);
        assertThat(countByStatus(words, WordStatus.SEED)).isEqualTo(13);
        assertThat(countByStatus(words, WordStatus.SPROUT)).isEqualTo(8);
        assertThat(countByStatus(words, WordStatus.BLOOM)).isEqualTo(4);
    }

    @Test
    void pool_backfillsFromOtherTiersWhenOneIsShort() {
        seedWords(aliceDictionary, WordStatus.SEED, 40, null);
        seedWords(aliceDictionary, WordStatus.SPROUT, 2, null);

        List<WordResponse> words = learningService.getDailyPool(alice, aliceDictionary).words();

        assertThat(words).hasSize(25);
        assertThat(countByStatus(words, WordStatus.SPROUT)).isEqualTo(2);
        assertThat(countByStatus(words, WordStatus.SEED)).isEqualTo(23);
    }

    @Test
    void pool_isTheSameSetWithinTheSameDay() {
        seedWords(aliceDictionary, WordStatus.SEED, 40, null);

        List<UUID> first = idsOf(learningService.getDailyPool(alice, aliceDictionary));
        List<UUID> second = idsOf(learningService.getDailyPool(alice, aliceDictionary));

        assertThat(second).isEqualTo(first);
    }

    @Test
    void pool_isScopedToOneDictionary() {
        UUID otherDictionary = dictionaryService
                .createDictionary(alice, new DictionaryCreateRequest("de"))
                .id();

        seedWords(aliceDictionary, WordStatus.SEED, 3, null);
        seedWords(otherDictionary, WordStatus.SEED, 5, null);

        assertThat(learningService.getDailyPool(alice, aliceDictionary).words()).hasSize(3);
        assertThat(learningService.getDailyPool(alice, otherDictionary).words()).hasSize(5);
    }

    @Test
    void seedWord_repeatsAfterOneToThreeDays() {
        Instant now = Instant.now();

        seedWords(aliceDictionary, WordStatus.SEED, 1, now.minus(Duration.ofHours(4)));
        seedWords(aliceDictionary, WordStatus.SEED, 1, now.minus(Duration.ofDays(4)));

        List<WordResponse> words = learningService.getDailyPool(alice, aliceDictionary).words();

        assertThat(words).hasSize(1);
        assertThat(words.getFirst().lastLearnedAt()).isBefore(now.minus(Duration.ofDays(3)));
    }

    @Test
    void sproutWord_repeatsAfterOneToTwoWeeks() {
        Instant now = Instant.now();

        seedWords(aliceDictionary, WordStatus.SPROUT, 1, now.minus(Duration.ofDays(3)));
        seedWords(aliceDictionary, WordStatus.SPROUT, 1, now.minus(Duration.ofDays(15)));

        List<WordResponse> words = learningService.getDailyPool(alice, aliceDictionary).words();

        assertThat(words).hasSize(1);
        assertThat(words.getFirst().lastLearnedAt()).isBefore(now.minus(Duration.ofDays(14)));
    }

    @Test
    void bloomWord_repeatsAfterAMonth() {
        Instant now = Instant.now();

        seedWords(aliceDictionary, WordStatus.BLOOM, 1, now.minus(Duration.ofDays(10)));
        seedWords(aliceDictionary, WordStatus.BLOOM, 1, now.minus(Duration.ofDays(31)));

        List<WordResponse> words = learningService.getDailyPool(alice, aliceDictionary).words();

        assertThat(words).hasSize(1);
        assertThat(words.getFirst().lastLearnedAt()).isBefore(now.minus(Duration.ofDays(30)));
    }

    @Test
    void successfulAttempt_shrinksTodaysPoolWithoutPullingInANewWord() {
        seedWords(aliceDictionary, WordStatus.SEED, 40, null);

        List<UUID> before = idsOf(learningService.getDailyPool(alice, aliceDictionary));

        learningService.submitResult(alice, before.getFirst(), true);

        LearningPoolResponse pool = learningService.getDailyPool(alice, aliceDictionary);

        assertThat(pool.words()).hasSize(24);
        assertThat(pool.completedToday()).isEqualTo(1);
        assertThat(idsOf(pool)).doesNotContain(before.getFirst()).isSubsetOf(before);
    }

    @Test
    void failedAttempt_keepsTheWordInTodaysPool() {
        seedWords(aliceDictionary, WordStatus.SEED, 3, null);

        UUID word = idsOf(learningService.getDailyPool(alice, aliceDictionary)).getFirst();

        LearningAnswerResponse answer = learningService.submitResult(alice, word, false);

        assertThat(answer.lastLearnedAt()).isNull();
        assertThat(idsOf(learningService.getDailyPool(alice, aliceDictionary))).contains(word);
    }

    @Test
    void successfulAttempt_addsOnePointAndMarksTheWordAsPractised() {
        UUID word = seedWords(aliceDictionary, WordStatus.SEED, 1, null).getFirst();

        LearningAnswerResponse answer = learningService.submitResult(alice, word, true);

        assertThat(answer.scores()).isEqualTo(1);
        assertThat(answer.status()).isEqualTo(WordStatus.SEED);
        assertThat(answer.previousStatus()).isEqualTo(WordStatus.SEED);
        assertThat(answer.lastLearnedAt()).isNotNull();
    }

    @Test
    void failedAttempt_removesOnePointButNeverGoesNegative() {
        UUID word = seedWords(aliceDictionary, WordStatus.SEED, 1, null).getFirst();

        assertThat(learningService.submitResult(alice, word, false).scores()).isZero();

        learningService.submitResult(alice, word, true);
        learningService.submitResult(alice, word, true);

        assertThat(learningService.submitResult(alice, word, false).scores()).isEqualTo(1);
    }

    @Test
    void reachingOneHundredPoints_promotesToSprout() {
        UUID word = wordWithScores(99, WordStatus.SEED);

        LearningAnswerResponse answer = learningService.submitResult(alice, word, true);

        assertThat(answer.scores()).isEqualTo(100);
        assertThat(answer.previousStatus()).isEqualTo(WordStatus.SEED);
        assertThat(answer.status()).isEqualTo(WordStatus.SPROUT);
    }

    @Test
    void reachingOneHundredFiftyPoints_promotesToBloom() {
        UUID word = wordWithScores(149, WordStatus.SPROUT);

        LearningAnswerResponse answer = learningService.submitResult(alice, word, true);

        assertThat(answer.scores()).isEqualTo(150);
        assertThat(answer.status()).isEqualTo(WordStatus.BLOOM);
    }

    @Test
    void droppingBelowOneHundredFifty_demotesToSprout() {
        UUID word = wordWithScores(150, WordStatus.BLOOM);

        LearningAnswerResponse answer = learningService.submitResult(alice, word, false);

        assertThat(answer.scores()).isEqualTo(149);
        assertThat(answer.previousStatus()).isEqualTo(WordStatus.BLOOM);
        assertThat(answer.status()).isEqualTo(WordStatus.SPROUT);
    }

    @Test
    void droppingBelowOneHundred_demotesToSeed() {
        UUID word = wordWithScores(100, WordStatus.SPROUT);

        LearningAnswerResponse answer = learningService.submitResult(alice, word, false);

        assertThat(answer.scores()).isEqualTo(99);
        assertThat(answer.status()).isEqualTo(WordStatus.SEED);
    }

    @Test
    void anotherUsersDictionary_isReportedAsMissing() {
        UUID bob = newUser("bob").getId();

        assertThatThrownBy(() -> learningService.getDailyPool(bob, aliceDictionary))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void anotherUsersWord_cannotBeScored() {
        UUID bob = newUser("bob").getId();
        UUID word = seedWords(aliceDictionary, WordStatus.SEED, 1, null).getFirst();

        assertThatThrownBy(() -> learningService.submitResult(bob, word, true))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(wordRepository.findById(word).orElseThrow().getScores()).isZero();
    }

    private UUID wordWithScores(int scores, WordStatus status) {
        Word word = newWord(aliceDictionary, status, null);
        word.setScores(scores);

        return wordRepository.save(word).getId();
    }

    private List<UUID> seedWords(UUID dictionaryId, WordStatus status, int count, Instant lastLearnedAt) {
        return wordRepository
                .saveAll(IntStream.range(0, count)
                        .mapToObj(index -> newWord(dictionaryId, status, lastLearnedAt))
                        .toList())
                .stream()
                .map(Word::getId)
                .toList();
    }

    private static Word newWord(UUID dictionaryId, WordStatus status, Instant lastLearnedAt) {
        Word word = new Word();
        word.setDictionaryId(dictionaryId);
        word.setWord("word-" + UUID.randomUUID());
        word.setDefinitions(new String[] {"definition"});
        word.setStatus(status);
        word.setLastLearnedAt(lastLearnedAt);

        return word;
    }

    private static List<UUID> idsOf(LearningPoolResponse pool) {
        return pool.words().stream().map(WordResponse::id).toList();
    }

    private static long countByStatus(List<WordResponse> words, WordStatus status) {
        return words.stream().filter(word -> word.status() == status).count();
    }

    private User newUser(String name) {
        String unique = name + "-" + UUID.randomUUID() + "@example.com";

        return userService.createUser(unique, name, "irrelevant-hash");
    }
}

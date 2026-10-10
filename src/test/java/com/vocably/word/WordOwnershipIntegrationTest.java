package com.vocably.word;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vocably.common.error.ResourceNotFoundException;
import com.vocably.dictionary.DictionaryService;
import com.vocably.dictionary.dto.DictionaryCreateRequest;
import com.vocably.support.PostgresIntegrationTest;
import com.vocably.user.User;
import com.vocably.user.UserService;
import com.vocably.word.dto.WordCreateRequest;

class WordOwnershipIntegrationTest extends PostgresIntegrationTest {

    private static final Pageable FIRST_PAGE = PageRequest.of(0, 20);

    @Autowired
    private UserService userService;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private WordService wordService;

    private UUID alice;
    private UUID bob;
    private UUID aliceDictionary;
    private UUID aliceWord;

    @BeforeEach
    void createTwoUsers() {
        alice = newUser("alice").getId();
        bob = newUser("bob").getId();

        aliceDictionary = dictionaryService
                .createDictionary(alice, new DictionaryCreateRequest("en"))
                .id();

        aliceWord = wordService
                .createWord(alice, new WordCreateRequest(aliceDictionary, "serendipity", new String[] {"luck"}))
                .id();
    }

    @Test
    void findById_doesNotReturnAnotherUsersWord() {
        assertThatThrownBy(() -> wordService.findById(bob, aliceWord))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_returnsTheOwnersOwnWord() {
        assertThat(wordService.findById(alice, aliceWord).word()).isEqualTo("serendipity");
    }

    @Test
    void getAll_isScopedToTheCaller() {
        assertThat(wordService.getAll(alice, FIRST_PAGE)).hasSize(1);
        assertThat(wordService.getAll(bob, FIRST_PAGE)).isEmpty();
    }

    @Test
    void search_doesNotMatchAnotherUsersWord() {
        assertThat(wordService.findByWord(alice, "serendipity", FIRST_PAGE)).hasSize(1);
        assertThat(wordService.findByWord(bob, "serendipity", FIRST_PAGE)).isEmpty();
    }

    @Test
    void search_isCaseInsensitive() {
        assertThat(wordService.findByWord(alice, "SeReNdIpItY", FIRST_PAGE)).hasSize(1);
    }

    @Test
    void listingAnotherUsersDictionary_isReportedAsMissing() {
        assertThatThrownBy(() -> wordService.findByDictionaryId(bob, aliceDictionary, FIRST_PAGE))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void creatingAWordInAnotherUsersDictionary_isRejected() {
        WordCreateRequest intrusion =
                new WordCreateRequest(aliceDictionary, "intruder", new String[] {"nope"});

        assertThatThrownBy(() -> wordService.createWord(bob, intrusion))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThat(wordService.getAll(alice, FIRST_PAGE)).hasSize(1);
    }

    private User newUser(String name) {
        String unique = name + "-" + UUID.randomUUID() + "@example.com";

        return userService.createUser(unique, name, "irrelevant-hash");
    }
}

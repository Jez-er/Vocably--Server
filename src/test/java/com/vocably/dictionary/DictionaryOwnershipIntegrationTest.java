package com.vocably.dictionary;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vocably.common.error.ConflictException;
import com.vocably.common.error.ResourceNotFoundException;
import com.vocably.dictionary.dto.DictionaryCreateRequest;
import com.vocably.support.PostgresIntegrationTest;
import com.vocably.user.User;
import com.vocably.user.UserService;

class DictionaryOwnershipIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private DictionaryService dictionaryService;

    private UUID alice;
    private UUID bob;
    private UUID aliceDictionary;

    @BeforeEach
    void createTwoUsers() {
        alice = newUser("alice").getId();
        bob = newUser("bob").getId();

        aliceDictionary = dictionaryService
                .createDictionary(alice, new DictionaryCreateRequest("en"))
                .id();
    }

    @Test
    void getById_doesNotReturnAnotherUsersDictionary() {
        assertThatThrownBy(() -> dictionaryService.getDictionaryById(bob, aliceDictionary))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByLanguage_isScopedToTheCaller() {
        assertThat(dictionaryService.getDictionaryByLanguageCode(alice, "en").id())
                .isEqualTo(aliceDictionary);

        assertThatThrownBy(() -> dictionaryService.getDictionaryByLanguageCode(bob, "en"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_listsOnlyTheCallersDictionaries() {
        assertThat(dictionaryService.getAllDictionaries(alice)).hasSize(1);
        assertThat(dictionaryService.getAllDictionaries(bob)).isEmpty();
    }

    @Test
    void isOwnedBy_isFalseForAnotherUser() {
        assertThat(dictionaryService.isOwnedBy(alice, aliceDictionary)).isTrue();
        assertThat(dictionaryService.isOwnedBy(bob, aliceDictionary)).isFalse();
    }

    @Test
    void twoUsersCanBothHaveTheSameLanguage() {
        assertThat(dictionaryService.createDictionary(bob, new DictionaryCreateRequest("en")).id())
                .isNotEqualTo(aliceDictionary);
    }

    @Test
    void sameLanguageTwiceForOneUser_conflicts() {
        assertThatThrownBy(() -> dictionaryService.createDictionary(alice, new DictionaryCreateRequest("en")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void unknownLanguage_isReportedAsMissingLanguage() {
        assertThatThrownBy(() -> dictionaryService.createDictionary(bob, new DictionaryCreateRequest("zz")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void languageCodeIsNormalised() {
        dictionaryService.createDictionary(bob, new DictionaryCreateRequest("  DE  "));

        assertThat(dictionaryService.getDictionaryByLanguageCode(bob, "de").languageCode())
                .isEqualTo("de");
    }

    private User newUser(String name) {
        return userService.createUser(name + "-" + UUID.randomUUID() + "@example.com", name, "hash");
    }
}

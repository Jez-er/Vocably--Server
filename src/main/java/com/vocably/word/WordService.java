package com.vocably.word;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service 
public class WordService {
	private final WordRepository wordRepository;

	public WordService(WordRepository wordRepository) {
		this.wordRepository = wordRepository;
	}

	public Word createWord(Word word) {
		return wordRepository.save(word);
	}

	public List<Word> getAll() {
		return wordRepository.findAll();
	}

	public Optional<List<Word>> findByWord(String word) {
		return wordRepository.findByWord(word);
	}

	public Optional<Word> findById(UUID id) {
		return wordRepository.findById(id);
	}

	public Optional<List<Word>> findByDictionaryId(String dictionaryId) {
		return wordRepository.findByDictionaryId(dictionaryId);
	}

}

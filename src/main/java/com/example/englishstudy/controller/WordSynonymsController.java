package com.example.englishstudy.controller;

import com.example.englishstudy.entity.WordSynonyms;
import com.example.englishstudy.service.WordSynonymsService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/word-synonyms")
public class WordSynonymsController {

    private final WordSynonymsService wordSynonymsService;

    public WordSynonymsController(WordSynonymsService wordSynonymsService) {
        this.wordSynonymsService = wordSynonymsService;
    }

    /**
     * 根据单词ID获取同义词信息
     * @param wordId 单词ID
     * @return 同义词信息列表
     */
    @GetMapping("/by-word-id/{wordId}")
    public Result<List<WordSynonyms>> getSynonymsByWordId(@PathVariable Integer wordId) {
        List<WordSynonyms> synonyms = wordSynonymsService.getWordSynonymsByWordId(wordId);
        return Result.success(synonyms);
    }

    /**
     * 根据单词获取同义词信息
     * @param word 单词
     * @return 同义词信息列表
     */
    @GetMapping("/by-word/{word}")
    public Result<List<WordSynonyms>> getSynonymsByWord(@PathVariable String word) {
        List<WordSynonyms> synonyms = wordSynonymsService.getWordSynonymsByWord(word);
        return Result.success(synonyms);
    }
}
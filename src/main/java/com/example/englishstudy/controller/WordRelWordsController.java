package com.example.englishstudy.controller;

import com.example.englishstudy.entity.WordRelWords;
import com.example.englishstudy.service.WordRelWordsService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/word-rel-words")
public class WordRelWordsController {

    private final WordRelWordsService wordRelWordsService;

    public WordRelWordsController(WordRelWordsService wordRelWordsService) {
        this.wordRelWordsService = wordRelWordsService;
    }

    /**
     * 根据单词ID获取相关单词信息
     * @param wordId 单词ID
     * @return 相关单词信息列表
     */
    @GetMapping("/by-word-id/{wordId}")
    public Result<List<WordRelWords>> getRelWordsByWordId(@PathVariable Integer wordId) {
        List<WordRelWords> relWords = wordRelWordsService.getWordRelWordsByWordId(wordId);
        return Result.success(relWords);
    }

    /**
     * 根据单词获取相关单词信息
     * @param word 单词
     * @return 相关单词信息列表
     */
    @GetMapping("/by-word/{word}")
    public Result<List<WordRelWords>> getRelWordsByWord(@PathVariable String word) {
        List<WordRelWords> relWords = wordRelWordsService.getWordRelWordsByWord(word);
        return Result.success(relWords);
    }
}

package com.example.englishstudy.controller;

import com.example.englishstudy.entity.Phrases;
import com.example.englishstudy.service.PhrasesService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/phrases")
public class PhrasesController {

    private final PhrasesService phrasesService;

    public PhrasesController(PhrasesService phrasesService) {
        this.phrasesService = phrasesService;
    }

    /**
     * 根据单词ID获取短语搭配信息
     * @param wordId 单词ID
     * @return 短语搭配信息列表
     */
    @GetMapping("/by-word-id/{wordId}")
    public Result<List<Phrases>> getPhrasesByWordId(@PathVariable Integer wordId) {
        List<Phrases> phrases = phrasesService.getPhrasesByWordId(wordId);
        return Result.success(phrases);
    }

    /**
     * 根据单词获取短语搭配信息
     * @param word 单词
     * @return 短语搭配信息列表
     */
    @GetMapping("/by-word/{word}")
    public Result<List<Phrases>> getPhrasesByWord(@PathVariable String word) {
        List<Phrases> phrases = phrasesService.getPhrasesByWord(word);
        return Result.success(phrases);
    }
}
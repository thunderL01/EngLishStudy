package com.example.englishstudy.controller;

import com.example.englishstudy.entity.Sentences;
import com.example.englishstudy.service.SentencesService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sentences")
public class SentencesController {

    private final SentencesService sentencesService;

    public SentencesController(SentencesService sentencesService) {
        this.sentencesService = sentencesService;
    }

    /**
     * 根据单词ID获取例句信息
     * @param wordId 单词ID
     * @return 例句信息列表
     */
    @GetMapping("/by-word-id/{wordId}")
    public Result<List<Sentences>> getSentencesByWordId(@PathVariable Integer wordId) {
        List<Sentences> sentences = sentencesService.getSentencesByWordId(wordId);
        return Result.success(sentences);
    }

    /**
     * 根据单词获取例句信息
     * @param word 单词
     * @return 例句信息列表
     */
    @GetMapping("/by-word/{word}")
    public Result<List<Sentences>> getSentencesByWord(@PathVariable String word) {
        List<Sentences> sentences = sentencesService.getSentencesByWord(word);
        return Result.success(sentences);
    }
}
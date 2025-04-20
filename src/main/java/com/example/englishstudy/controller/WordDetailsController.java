package com.example.englishstudy.controller;

import com.example.englishstudy.entity.WordDetails;
import com.example.englishstudy.service.WordDetailsService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/word-details")
public class WordDetailsController {

    private final WordDetailsService wordDetailsService;

    public WordDetailsController(WordDetailsService wordDetailsService) {
        this.wordDetailsService = wordDetailsService;
    }

    /**
     * 根据单词ID获取单词详情
     * @param wordId 单词ID
     * @return 单词详情列表
     */
    @GetMapping("/by-word-id/{wordId}")
    public Result<List<WordDetails>> getDetailsByWordId(@PathVariable Integer wordId) {
        List<WordDetails> details = wordDetailsService.getWordDetailsByWordId(wordId);
        return Result.success(details);
    }

    /**
     * 根据单词获取单词详情
     * @param word 单词
     * @return 单词详情列表
     */
    @GetMapping("/by-word/{word}")
    public Result<List<WordDetails>> getDetailsByWord(@PathVariable String word) {
        List<WordDetails> details = wordDetailsService.getWordDetailsByWord(word);
        return Result.success(details);
    }
}
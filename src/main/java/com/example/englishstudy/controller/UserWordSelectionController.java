package com.example.englishstudy.controller;


import com.example.englishstudy.service.UserWordSelectionService;
import com.example.englishstudy.service.WordService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-word-selection")
public class UserWordSelectionController {

    private final UserWordSelectionService userWordSelectionService;
    private final WordService wordService;

    public UserWordSelectionController(UserWordSelectionService userWordSelectionService, WordService wordService) {
        this.userWordSelectionService = userWordSelectionService;
        this.wordService = wordService;
    }

    /**
     * 用户选择单词进行记忆
     * @param userId 用户ID
     * @param words 单词ID列表
     * @param dailyStudyAmount 每日学习量
     * @return 操作结果
     */
    @PostMapping("/select")
    public Result<Boolean> selectWordsForMemory(
            @RequestParam Integer userId,
            @RequestParam List<String> words,
            @RequestParam Integer dailyStudyAmount) {
        boolean success = userWordSelectionService.selectWordsForMemory(userId, words, dailyStudyAmount);
        return Result.success(success);
    }

    /**
     * 获取用户在指定前缀词典中已选择的单词
     * @param userId 用户ID
     * @param bookIdPrefix 词典前缀
     * @return 单词列表
     */
    @GetMapping("/selected-by-prefix")
    public Result<List<String>> getSelectedWordsByPrefix(
            @RequestParam Integer userId,
            @RequestParam String bookIdPrefix) {
        List<String> words = userWordSelectionService.getSelectedWordsByBookIdPrefix(userId, bookIdPrefix);
        return Result.success(words);
    }

    @GetMapping("/all-words-by-prefix")
    public Result<Map<String, List<String>>> getAllWordsByPrefix(
            @RequestParam Integer userId,
            @RequestParam String bookIdPrefix) {
        List<String> allWords = wordService.getWordsByBookIdPrefix(bookIdPrefix);
        List<String> selectedWords = userWordSelectionService.getSelectedWordsByBookIdPrefix(userId, bookIdPrefix);
        Map<String, List<String>> result = new HashMap<>();
        result.put("allWords", allWords);
        result.put("selectedWords", selectedWords);
        return Result.success(result);
    }



    /**
     * 从指定单词列表中随机选择单词进行记忆
     * @param userId 用户ID
     * @param words 单词ID列表
     * @param dailyStudyAmount 每日学习量
     * @return 操作结果
     */
    @PostMapping("/random")
    public Result<List<String>> selectRandomWords(
            @RequestParam Integer userId,
            @RequestParam List<String> words,
            @RequestParam Integer dailyStudyAmount) {
        List<String> selectedWords = userWordSelectionService.getRandomWordsByUserIdAndAmount(userId, words, dailyStudyAmount);
        return Result.success(selectedWords);
    }

    @GetMapping("/count-selected")
    public Result<Integer> countSelectedWords(@RequestParam Integer userId) {
        int count = userWordSelectionService.countSelectedWordsByUserId(userId);
        return Result.success(count);
    }

}
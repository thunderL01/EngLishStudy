package com.example.englishstudy.controller;

import com.example.englishstudy.entity.Word;
import com.example.englishstudy.service.WordService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    /**
     * 根据单词ID获取单词信息
     * @param wordId 单词ID
     * @return 单词详细信息
     */
    @GetMapping("/{wordId}")
    public Result<Word> getWordById(@PathVariable Integer wordId) {
        Word word = wordService.getWordById(wordId);
        return Result.success(word);
    }

    /**
     * 根据单词获取单词信息
     * @param word 单词
     * @return 单词详细信息
     */
    @GetMapping("/by-word/{word}")
    public Result<Word> getWordByWord(@PathVariable String word) {
        Word wordInfo = wordService.getWordByWord(word);
        return Result.success(wordInfo);
    }

    /**
     * 获取所有不同的书籍ID
     * @return 书籍ID列表
     */
    @GetMapping("/book-ids")
    public Result<List<String>> getAllBookIds() {
        List<String> bookIds = wordService.getAllDistinctBookIds();
        return Result.success(bookIds);
    }

    /**
     * 根据书籍ID获取单词数量
     * @param bookId 书籍ID
     * @return 单词数量
     */
    @GetMapping("/count/{bookId}")
    public Result<Integer> getWordCountByBookId(@PathVariable String bookId) {
        int count = wordService.getWordCountByBookId(bookId);
        return Result.success(count);
    }


    /**
     * 根据书籍ID前缀获取单词数量
     * @param bookIdPrefix 书籍ID
     * @return 单词数量
     */
    @GetMapping("/counts/{bookIdPrefix}")
    public Result<Integer> getWordCountByBookIdPrefix(@PathVariable String bookIdPrefix) {
        int count = wordService.getWordCountByBookIdPrefix(bookIdPrefix);
        return Result.success(count);
    }


    /**
     * 根据书籍ID获取单词列表
     * @param bookId 书籍ID
     * @return 单词列表
     */
    @GetMapping("/by-book/{bookId}")
    public Result<List<String>> getWordsByBookId(@PathVariable String bookId) {
        List<String> words = wordService.getWordsByBookId(bookId);
        return Result.success(words);
    }


    /**
     * 根据书籍ID前缀获取单词列表
     * @param bookIdPrefix 书籍ID前缀
     * @return 单词列表
     */
    @GetMapping("/by-book-prefix/{bookIdPrefix}")
    public Result<List<String>> getWordsByBookIdPrefix(@PathVariable String bookIdPrefix) {
        List<String> words = wordService.getWordsByBookIdPrefix(bookIdPrefix);
        return Result.success(words);
    }


    /**
     * 单词前缀搜索
     * @param keyword 搜索关键词
     * @param limit 返回数量（默认10）
     */
    @GetMapping("/suggestions")
    public Result<List<String>> getSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") int limit) {

        if (StringUtils.isBlank(keyword)) {
            return Result.success(Collections.emptyList());
        }

        return Result.success(wordService.searchByPrefix(keyword, limit));
    }

    /**
     * 获取所有单词
     * @return 所有单词列表
     */
    @GetMapping("/all")
    public Result<List<String>> getAllWords() {
        List<String> words = wordService.getAllWords();
        return Result.success(words);
    }


}
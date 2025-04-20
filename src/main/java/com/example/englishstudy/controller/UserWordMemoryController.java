package com.example.englishstudy.controller;

import com.example.englishstudy.service.UserWordMemoryService;
import com.example.englishstudy.utils.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/user-word-memory")
public class UserWordMemoryController {

    private final UserWordMemoryService userWordMemoryService;

    public UserWordMemoryController(UserWordMemoryService userWordMemoryService) {
        this.userWordMemoryService = userWordMemoryService;
    }

    /**
     * 创建记忆记录
     */
    @PostMapping("/create")
    public Result<Boolean> createMemoryRecord(
            @RequestParam Integer userId,
            @RequestParam Integer wordId) {
        boolean success = userWordMemoryService.createMemoryRecord(userId, wordId, LocalDate.now());
        return Result.success(success);
    }

    /**
     * 创建复习记录
     * @param userId 用户ID
     * @param wordId 单词ID

     * @return 操作结果
     */
    @PostMapping("/review-record")
    public Result<Boolean> createReviewRecord(
            @RequestParam Integer userId,
            @RequestParam Integer wordId) {
        boolean success = userWordMemoryService.createReviewRecord(userId, wordId);
        return Result.success(success);
    }


    @GetMapping("/memory-strength-stats")
    public Result<Map<LocalDate, Double>> getDailyAverageMemoryStrength(
            @RequestParam Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

            Map<LocalDate, Double> stats = userWordMemoryService.getDailyAverageMemoryStrength(userId, startDate, endDate);
            return Result.success(stats);

    }



}
package com.example.englishstudy.controller;

import com.example.englishstudy.entity.Word;
import com.example.englishstudy.service.UserWordStudyStatusService;
import com.example.englishstudy.utils.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-word-study-status")
public class UserWordStudyStatusController {

    private final UserWordStudyStatusService userWordStudyStatusService;

    public UserWordStudyStatusController(UserWordStudyStatusService userWordStudyStatusService) {
        this.userWordStudyStatusService = userWordStudyStatusService;
    }

    /**
     * 记录单词学习状态（且如果是第一次学习时，同时在userWordProgress创建学习状态记录）
     * @param userId 用户ID
     * @param wordId 单词ID
     * @param studyStatus 学习状态("认识"/"模糊"/"忘记")
     * @return 操作结果
     */
    @PostMapping("/record")
    public Result<Boolean> recordLearningStatus(
            @RequestParam Integer userId,
            @RequestParam Integer wordId,
            @RequestParam String studyStatus) {
        boolean success = userWordStudyStatusService.recordLearning(userId, wordId, studyStatus);
        return Result.success(success);
    }

    /**
     * 获取需要学习的单词数量
     * @param userId 用户ID
     * @param dailyStudyAmount 每日学习量
     * @return 单词数量
     */
    @GetMapping("/count-to-learn")
    public Result<Integer> countWordsToLearn(
            @RequestParam Integer userId,
            @RequestParam int dailyStudyAmount) {
        int count = userWordStudyStatusService.countWordsToLearn(userId, dailyStudyAmount);
        return Result.success(count);
    }


    /**
     * 获取日期范围内的新学单词统计
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期到数量的映射
     */
    @GetMapping("/stats/new-study")
    public Result<Map<LocalDate, Integer>> getNewStudyStats(
            @RequestParam Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<LocalDate, Integer> stats = userWordStudyStatusService.getNewStudyStats(userId, startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 获取日期范围内的复习单词统计
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期到数量的映射
     */
    @GetMapping("/stats/review")
    public Result<Map<LocalDate, Integer>> getReviewStats(
            @RequestParam Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<LocalDate, Integer> stats = userWordStudyStatusService.getReviewStats(userId, startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 获取按优先级排序的待学习单词
     * @param userId 用户ID
     * @return 单词
     */
    @GetMapping("/words-to-learn")
    public Result<String> getWordsToLearnByPriority(
            @RequestParam Integer userId) {
        String word = userWordStudyStatusService.getWordsToLearnByPriority(userId);
        return Result.success(word);
    }


    /**
     * 统计用户当天未完成学习的单词数量
     * @param userId 用户ID
     * @param dailyStudyAmount 每日学习量
     * @return 单词数量
     */
    @GetMapping("/count-pending-review")
    public Result<Integer> countPendingReviewWords(
            @RequestParam Integer userId,
            @RequestParam int dailyStudyAmount) {
        int count = userWordStudyStatusService.countPendingReviewWordsByUserId(userId, dailyStudyAmount);
        return Result.success(count);
    }
}

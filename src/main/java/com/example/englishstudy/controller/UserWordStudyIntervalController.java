package com.example.englishstudy.controller;

import com.example.englishstudy.service.UserWordStudyIntervalService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user-word-study-interval")
public class UserWordStudyIntervalController {

    private final UserWordStudyIntervalService userWordStudyIntervalService;

    public UserWordStudyIntervalController(UserWordStudyIntervalService userWordStudyIntervalService) {
        this.userWordStudyIntervalService = userWordStudyIntervalService;
    }

    /**
     * 记录单词学习间隔
     * @param userId 用户ID
     * @param wordId 单词ID
     * @param studyStatus 学习状态("认识"/"模糊"/"忘记")
     * @return 操作结果
     */
    @PostMapping("/record")
    public Result<Boolean> recordStudyInterval(
            @RequestParam Integer userId,
            @RequestParam Integer wordId,
            @RequestParam String studyStatus) {
        boolean success = userWordStudyIntervalService.recordStudyInterval(userId, wordId, studyStatus);
        return Result.success(success);
    }

    /**
     * 减少用户所有单词的学习间隔剩余天数
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/decrement-intervals")
    public Result<Boolean> decrementIntervals(
            @RequestParam Integer userId) {
        boolean success = userWordStudyIntervalService.decrementIntervals(userId, LocalDateTime.now());
        return Result.success(success);
    }


}
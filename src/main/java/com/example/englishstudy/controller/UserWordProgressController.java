package com.example.englishstudy.controller;

import com.example.englishstudy.entity.UserWordProgress;
import com.example.englishstudy.service.UserWordProgressService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user-word-progress")
public class UserWordProgressController {

    private final UserWordProgressService userWordProgressService;

    public UserWordProgressController(UserWordProgressService userWordProgressService) {
        this.userWordProgressService = userWordProgressService;
    }

    /**
     * 记录用户学习单词的进度
     * @param userId 用户ID
     * @param wordId 单词ID
     * @return 操作结果
     */
    @PostMapping("/record")
    public Result<Boolean> recordLearningProgress(
            @RequestParam Integer userId,
            @RequestParam Integer wordId) {
        boolean success = userWordProgressService.recordLearningProgress(userId, wordId, LocalDate.now());
        return Result.success(success);
    }

    /**
     * 获取用户对某个单词的学习历史记录(按学习时间倒序)
     * @param userId 用户ID
     * @param wordId 单词ID
     * @return 学习历史记录列表
     */
    @GetMapping("/history")
    public Result<List<UserWordProgress>> getProgressHistory(
            @RequestParam Integer userId,
            @RequestParam Integer wordId) {
        List<UserWordProgress> history = userWordProgressService.getProgressHistory(userId, wordId);
        return Result.success(history);
    }
}

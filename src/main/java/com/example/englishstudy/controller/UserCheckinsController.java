package com.example.englishstudy.controller;

import com.example.englishstudy.entity.UserCheckins;
import com.example.englishstudy.service.UserCheckinsService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkins")
public class UserCheckinsController {

    private final UserCheckinsService userCheckinsService;

    public UserCheckinsController(UserCheckinsService userCheckinsService) {
        this.userCheckinsService = userCheckinsService;
    }

    /**
     * 用户签到
     * @param userId 用户ID
     * @return 签到结果
     */
    @PostMapping("/{userId}")
    public Result<Boolean> checkin(@PathVariable Integer userId) {
        boolean success = userCheckinsService.checkin(userId);
        return Result.success(success);
    }

    @GetMapping("/history")
    public Result<List<UserCheckins>> getCheckinHistory(
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer days) {
        List<UserCheckins> history = userCheckinsService.getCheckinHistory(userId, days);
        return Result.success(history);
    }

    /**
     * 获取用户累计签到天数
     * @param userId 用户ID
     * @return 累计签到天数
     */
    @GetMapping("/total/{userId}")
    public Result<Integer> getTotalCheckinDays(@PathVariable Integer userId) {
        return Result.success(userCheckinsService.getTotalCheckinDays(userId));
    }

    /**
     * 获取用户连续签到天数
     * @param userId 用户ID
     * @return 连续签到天数
     */
    @GetMapping("/consecutive/{userId}")
    public Result<Integer> getConsecutiveCheckinDays(@PathVariable Integer userId) {
        return Result.success(userCheckinsService.getConsecutiveCheckinDays(userId));
    }
}
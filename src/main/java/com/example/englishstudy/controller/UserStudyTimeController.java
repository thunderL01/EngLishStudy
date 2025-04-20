package com.example.englishstudy.controller;

import com.example.englishstudy.service.UserStudyTimeService;
import com.example.englishstudy.utils.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-study-time")
public class UserStudyTimeController {

    private final UserStudyTimeService userStudyTimeService;

    public UserStudyTimeController(UserStudyTimeService userStudyTimeService) {
        this.userStudyTimeService = userStudyTimeService;
    }

    /**
     * 获取用户总学习时间
     * @param userId 用户ID
     * @return 总学习时间(秒)
     */
    @GetMapping("/total/{userId}")
    public Result<Integer> getTotalStudyTime(@PathVariable Integer userId) {
        Integer totalTime = userStudyTimeService.getTotalStudyTimeByUserId(userId);
        return Result.success(totalTime);
    }

    /**
     * 添加学习时间记录
     * @param userId 用户ID
     * @param studyTime 学习时间(秒)
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<Boolean> addStudyTime(
            @RequestParam Integer userId,
            @RequestParam Integer studyTime) {
        boolean success = userStudyTimeService.addStudyTimeRecord(userId, studyTime);
        return Result.success(success);
    }

    /**
     * 更新学习时间记录
     * @param userId 用户ID
     * @param studyTime 学习时间(秒)
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<Boolean> updateStudyTime(
            @RequestParam Integer userId,
            @RequestParam Integer studyTime) {
        boolean success = userStudyTimeService.updateStudyTimeRecord(userId, studyTime);
        return Result.success(success);
    }
}

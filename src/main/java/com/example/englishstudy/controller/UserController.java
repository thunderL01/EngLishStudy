package com.example.englishstudy.controller;


import com.example.englishstudy.entity.User;
import com.example.englishstudy.enums.AppearanceMode;
import com.example.englishstudy.enums.LearningMode;
import com.example.englishstudy.service.UserService;
import com.example.englishstudy.utils.LoginResult;
import com.example.englishstudy.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.englishstudy.utils.JwtUtils;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register/wechat")
    public Result<LoginResult> registerByWeChat(
            @RequestParam("code") String code,
            @RequestParam("nickName") String nickName) {
        User user = userService.registerUserByWeChat(code, nickName);
        if (user != null) {
            // 生成 JWT
            String token = jwtUtils.generateToken(user.getWechatOpenid());
            LoginResult loginResult = new LoginResult(user, token);
            return Result.success(loginResult);
        } else {
            return Result.error(Result.Code.INTERNAL_SERVER_ERROR, "微信登录失败");
        }
    }


    // 获取用户信息
    @GetMapping("/{userId}")
    public Result<User> getUserById(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        return user != null ? Result.success(user) : Result.error(Result.Code.NOT_FOUND);
    }

    // 设置用户头像
    @PostMapping("/avatar/set")
    public Result<Boolean> setUserAvatar(@RequestParam Integer userId,
                                         @RequestParam String avatar) {
        boolean success = userService.setUserAvatar(userId, avatar);
        return Result.success(success);
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestParam Integer userId,
            @RequestParam MultipartFile file) {
        try {
            // 1. 上传文件
            String relativePath = userService.uploadAvatar(file);

            // 2. 拼接完整URL
            String fullUrl = "http://localhost:8080" + relativePath;

            // 3. 更新用户头像URL
            boolean success = userService.setUserAvatar(userId, fullUrl);

            return success ?
                    Result.success(fullUrl) : // 使用你的Result.success(T data)
                    Result.error(Result.Code.NOT_FOUND, "用户不存在");

        } catch (IllegalArgumentException e) {
            return Result.error(Result.Code.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            return Result.error(Result.Code.INTERNAL_SERVER_ERROR, "文件存储失败");
        }
    }

    // 设置用户名
    @PostMapping("/username")
    public Result<Boolean> setUserName(@RequestParam Integer userId,
                                    @RequestParam String newUserName) {
        boolean success = userService.setUserName(userId, newUserName);
        return Result.success(success);
    }

    // 设置用户学习词典
    @PostMapping("/book")
    public Result<Boolean> setUserStudyBookId(@RequestParam Integer userId,
                                           @RequestParam String bookId) {
        boolean success = userService.setUserStudyBookId(userId, bookId);
        return Result.success(success);
    }

    //设置用户信息
    @PostMapping("/info")
    public Result<Boolean> updateUserInfo(@RequestParam Integer userId,
                                       @RequestParam(required = false) Integer age,
                                       @RequestParam(required = false) String gender,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) String birthday) {
        LocalDate birthDate = birthday != null ? LocalDate.parse(birthday) : null;
        boolean success = userService.updateUserInformation(userId, age, gender, phone, birthDate);
        return Result.success(success);
    }

    // 设置用户每日学习量
    @PostMapping("/settings/learning")
    public Result<Boolean> updateLearningSettings(@RequestParam Integer userId,
                                               @RequestParam(required = false) Integer dailyStudyAmount) {
        boolean success = userService.updateUserLearningSettings(userId, dailyStudyAmount);
        return Result.success(success);
    }

    // 设置用户学习模式
    @PostMapping("/settings/mode")
    public Result<Boolean> updateLearningMode(@RequestParam Integer userId,
                                           @RequestParam LearningMode learningMode) {
        boolean success = userService.updateLearningMode(userId, learningMode);
        return Result.success(success);
    }

    // 设置用户外观模式
    @PostMapping("/settings/appearance")
    public Result<Boolean> updateAppearanceMode(@RequestParam Integer userId,
                                             @RequestParam AppearanceMode appearanceMode) {
        boolean success = userService.updateAppearanceMode(userId, appearanceMode);
        return Result.success(success);
    }

    // 获取用户经验值
    @GetMapping("/experience/{userId}")
    public Result<Integer> getUserExperience(@PathVariable Integer userId) {
        Integer experience = userService.getUserExperience(userId);
        return Result.success(experience);
    }

    // 获取用户等级
    @GetMapping("/level/{userId}")
    public Result<Integer> getUserLevel(@PathVariable Integer userId) {
        int level = userService.getUserLevel(userId);
        return Result.success(level);
    }

    @GetMapping("/avatar/{userId}")
    public Result<String> getUserAvatar(@PathVariable Integer userId) {
        String avatar = userService.getUserAvatar(userId);
        return Result.success(avatar);
    }

    @GetMapping("/name/{userId}")
    public Result<String> getUserName(@PathVariable Integer userId) {
        String name = userService.getUserName(userId);
        return Result.success(name);
    }

    @GetMapping("/learning-mode/{userId}")
    public Result<LearningMode> getLearningMode(@PathVariable Integer userId) {
        LearningMode learningMode = userService.getLearningMode(userId);
        return Result.success(learningMode);
    }

    @GetMapping("/appearance-mode/{userId}")
    public Result<AppearanceMode> getAppearanceMode(@PathVariable Integer userId) {
        AppearanceMode appearanceMode = userService.getAppearanceMode(userId);
        return Result.success(appearanceMode);
    }

    @GetMapping("/learning-settings/{userId}")
    public Result<Integer> getUserLearningSettings(@PathVariable Integer userId) {
        Integer settings = userService.getUserLearningSettings(userId);
        return Result.success(settings);
    }



}

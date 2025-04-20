package com.example.englishstudy.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.englishstudy.entity.User;
import com.example.englishstudy.enums.AppearanceMode;
import com.example.englishstudy.enums.LearningMode;
import com.example.englishstudy.mapper.UserMapper;
import com.example.englishstudy.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${wechat.app-id}")
    private String APP_ID;

    @Value("${wechat.app-secret}")
    private String SECRET;


    @Value("${file.upload-dir}")
    private String uploadDir;

    // 允许的文件类型
    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png");

    @Override
    public User registerUserByPhone(String phoneNumber, String verificationCode) {
        return null;
    }

    @Override
    public User registerUserByWeChat(String code, String nickName) {
        // 基础参数校验
        if (StringUtils.isEmpty(code)) {
            logger.warn("微信登录失败: 前端传来的 code 为空");
            return null;
        }
        if (StringUtils.isEmpty(nickName)) {
            logger.warn("微信登录失败: 微信昵称为空");
            return null;
        }

        // 安全校验
        if (!isValidCode(code)) {
            logger.warn("微信登录失败: code 格式不合法");
            return null;
        }
        if (!isValidNickName(nickName)) {
            logger.warn("微信登录失败: 昵称包含非法字符");
            return null;
        }

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + APP_ID
                + "&secret=" + SECRET
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        try {
            HttpResponse response = httpClient.execute(httpGet);
            String result = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(result, HashMap.class);

            // 检查微信接口返回的错误
            if (map.containsKey("errcode")) {
                logger.error("微信登录失败: 微信接口返回错误码，错误信息: {}", map.get("errmsg"));
                return null;
            }

            // 获取 openid
            if (map.containsKey("openid")) {
                String openid = (String) map.get("openid");

                // 检查用户是否已存在
                Optional<User> userOptional = this.lambdaQuery()
                        .eq(User::getWechatOpenid, openid)
                        .oneOpt();

                User user;
                if (userOptional.isPresent()) {
                    user = userOptional.get();
                    logger.info("微信登录成功: 用户已存在，openid={}", openid);
                } else {
                    // 新用户注册
                    user = new User();
                    user.setWechatOpenid(openid);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUserName(nickName);
                    // 默认外观模式

                    if (this.save(user)) {
                        logger.info("微信登录注册成功: openid={}, nickName={}", openid, nickName);
                    } else {
                        logger.error("微信登录注册失败: 保存用户信息到数据库失败，openid={}", openid);
                        return null;
                    }
                }
                return user;
            } else {
                logger.error("微信登录失败: 未获取到 openid，返回结果: {}", result);
                return null;
            }
        } catch (IOException | JsonSyntaxException e) {
            logger.error("微信登录失败: 与微信服务器通信异常", e);
            return null;
        }
    }


    // 新增校验方法
    private boolean isValidCode(String code) {
        // 微信code通常是32位字符串
        return code != null && code.matches("^[a-zA-Z0-9]{32}$");
    }

    private boolean isValidNickName(String nickName) {
        // 允许中文、英文、数字和常见标点，长度2-20
        return nickName != null &&
                nickName.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9 ，。、；：！？\"'-.()（）]{2,20}$");
    }


    @Override
    public User getUserById(Integer userId) {
        if (userId == null) {
            logger.warn("获取用户信息失败: 用户ID为空");
            return null;
        }

        try {
            User user = getById(userId);
            if (user == null) {
                logger.warn("获取用户信息失败: 用户不存在, userId={}", userId);
            } else {
                logger.info("成功获取用户信息: userId={}", userId);
            }
            return user;
        } catch (Exception e) {
            logger.error("获取用户信息异常: userId={}", userId, e);
            return null;
        }
    }

    @Override
    public boolean setUserAvatar(Integer userId, String avatar) {
        if (userId == null || avatar == null) {
            logger.warn("设置头像失败: 参数为空, userId={}, avatar={}", userId, avatar);
            return false;
        }

        try {
            boolean success = update(new UpdateWrapper<User>()
                    .eq("user_id", userId)
                    .set("avatar", avatar));

            if (success) {
                logger.info("用户头像设置成功: userId={}", userId);
            } else {
                logger.warn("用户头像设置失败: 用户不存在, userId={}", userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("用户头像设置异常: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file) throws IOException {
        // 1. 校验文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 2. 校验文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("只支持JPEG/PNG格式图片");
        }

        // 3. 创建存储目录（如果不存在）
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 4. 生成唯一文件名（UUID + 文件扩展名）
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID() + fileExtension;

        // 5. 存储文件
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 返回相对路径（如：/uploads/filename.jpg）
        return "/uploads/" + newFilename;
    }


    @Override
    public boolean setUserName(Integer userId, String newUserName) {
        logger.info("尝试设置用户名: userId={}, newUserName={}", userId, newUserName);
        if (userId == null || newUserName == null) {
            logger.warn("设置用户名失败: 参数为空");
            return false;
        }

        try {
            boolean success = update(new UpdateWrapper<User>()
                    .eq("user_id", userId)
                    .set("user_name", newUserName));

            if (success) {
                logger.info("用户名设置成功: userId={}", userId);
            } else {
                logger.warn("用户名设置失败: 用户不存在, userId={}", userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("用户名设置异常: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public boolean setUserStudyBookId(Integer userId, String bookId) {
        logger.info("尝试设置用户学习书籍: userId={}, bookId={}", userId, bookId);

        try {
            boolean success = update(new UpdateWrapper<User>()
                    .eq("user_id", userId)
                    .set("book_id", bookId));

            if (success) {
                logger.info("学习书籍设置成功: userId={}, bookId={}", userId, bookId);
            } else {
                logger.warn("学习书籍设置失败: 用户不存在, userId={}", userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("学习书籍设置异常: userId={}", userId, e);
            return false;
        }

    }





    @Override
    public boolean updateUserInformation(Integer userId, Integer age, String gender, String phone, LocalDate birthday) {
        if (userId == null) {
            logger.warn("更新用户信息失败: 用户ID为空");
            return false;
        }

        try {
            UpdateWrapper<User> wrapper = new UpdateWrapper<User>().eq("user_id", userId);
            if (age != null) wrapper.set("age", age);
            if (gender != null) wrapper.set("gender", gender);
            if (phone != null) wrapper.set("phone", phone);
            if (birthday != null) wrapper.set("birthday", birthday);

            boolean success = update(wrapper);
            if (success) {
                logger.info("用户信息更新成功: userId={}", userId);
            } else {
                logger.warn("用户信息更新失败: 用户不存在, userId={}", userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("用户信息更新异常: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public boolean updateUserLearningSettings(Integer userId, Integer dailyStudyAmount) {
        logger.info("尝试更新用户学习设置: userId={}, dailyStudyAmount={}",
                userId, dailyStudyAmount);

        if (userId == null) {
            logger.warn("更新学习设置失败: 用户ID为空");
            return false;
        }

        if (dailyStudyAmount != null && dailyStudyAmount < 0) {
            logger.warn("更新学习设置失败: 每日学习量不能为负数, dailyStudyAmount={}", dailyStudyAmount);
            return false;
        }

        try {
            UpdateWrapper<User> wrapper = new UpdateWrapper<User>().eq("user_id", userId);
            if (dailyStudyAmount != null) wrapper.set("daily_study_amount", dailyStudyAmount);

            boolean success = update(wrapper);
            if (success) {
                logger.info("学习设置更新成功: userId={}", userId);
            } else {
                logger.warn("学习设置更新失败: 用户不存在, userId={}", userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("学习设置更新异常: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public int getUserLearningSettings(Integer userId) {
        logger.info("获取用户学习设置: userId={}", userId);

        try {
            User user = getById(userId);
            if (user == null) {
                logger.warn("获取学习设置失败: 用户不存在, userId={}", userId);
                return 0;
            }

            Integer dailyStudyAmount = user.getDailyStudyAmount();
            if (dailyStudyAmount == null) {
                logger.warn("获取学习设置: 用户每日学习量为空, 返回默认值0, userId={}", userId);
                return 0;
            }

            logger.info("成功获取用户学习设置: userId={}, dailyStudyAmount={}", userId, dailyStudyAmount);
            return dailyStudyAmount;
        } catch (Exception e) {
            logger.error("获取学习设置异常: userId={}", userId, e);
            return 0;
        }
    }


    @Override
    public boolean updateLearningMode(Integer userId, LearningMode learningMode) {
        logger.info("尝试更新学习模式: userId={}, learningMode={}", userId, learningMode);

        if (userId == null || learningMode == null) {
            logger.warn("更新学习模式失败: 参数为空");
            return false;
        }

        try {
            boolean success = update(new UpdateWrapper<User>()
                    .eq("user_id", userId)
                    .set("learning_mode", learningMode.name()));

            if (success) {
                logger.info("学习模式更新成功: userId={}, mode={}", userId, learningMode);
            } else {
                logger.warn("学习模式更新失败: 用户不存在, userId={}", userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("学习模式更新异常: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public boolean updateAppearanceMode(Integer userId, AppearanceMode appearanceMode) {
        logger.info("尝试更新外观模式: userId={}, appearanceMode={}", userId, appearanceMode);

        if (userId == null || appearanceMode == null) {
            logger.warn("更新外观模式失败: 参数为空");
            return false;
        }

        try {
            boolean success = update(new UpdateWrapper<User>()
                    .eq("user_id", userId)
                    .set("appearance_mode", appearanceMode.name()));

            if (success) {
                logger.info("外观模式更新成功: userId={}, mode={}", userId, appearanceMode);
            } else {
                logger.warn("外观模式更新失败: 用户不存在, userId={}", userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("外观模式更新异常: userId={}", userId, e);
            return false;
        }
    }


    @Override
    public Integer getUserExperience(Integer userId) {
        logger.info("获取用户经验值: userId={}", userId);

        if (userId == null) {
            logger.warn("获取经验值失败: 用户ID为空");
            return null;
        }

        try {
            Optional<User> userOptional = this.lambdaQuery()
                    .eq(User::getUserId, userId)
                    .oneOpt();

            Integer experience = userOptional.map(User::getExperience).orElse(null);
            if (experience == null) {
                logger.warn("获取经验值失败: 用户不存在或经验值为空, userId={}", userId);
            }
            logger.info("成功获取用户经验: userId={}, experience={}", userId, experience);
            return experience;
        } catch (Exception e) {
            logger.error("获取经验值异常: userId={}", userId, e);
            return null;
        }
    }

    @Override
    public int getUserLevel(Integer userId) {
        logger.info("获取用户等级: userId={}", userId);

        if (userId == null) {
            logger.warn("获取用户等级失败: 用户ID为空");
            return 1;
        }

        try {
            Integer level = baseMapper.getUserLevel(userId);
            if (level == null) {
                logger.warn("获取用户等级失败: 用户不存在, 返回默认等级1, userId={}", userId);
                return 1;
            }
            logger.info("成功获取用户等级: userId={}, level={}", userId, level);
            return level;
        } catch (Exception e) {
            logger.error("获取用户等级异常: userId={}", userId, e);
            return 1;
        }
    }


    @Override
    public String getUserAvatar(Integer userId) {
        if (userId == null) {
            logger.warn("获取用户头像失败: 用户ID为空");
            return null;
        }
        User user = this.getById(userId);
        logger.info("成功获取用户等级: userId={}, avatar={}", userId, user.getAvatar());
        return user != null ? user.getAvatar() : null;
    }

    @Override
    public String getUserName(Integer userId) {
        if (userId == null) {
            logger.warn("获取用户名失败: 用户ID为空");
            return null;
        }
        User user = this.getById(userId);
        logger.info("成功获取用户等级: userId={}, userName={}", userId, user.getUserName());
        return user != null ? user.getUserName() : null;
    }

    @Override
    public LearningMode getLearningMode(Integer userId) {
        if (userId == null) {
            logger.warn("获取学习模式失败: 用户ID为空");
            return null;
        }
        User user = this.getById(userId);
        logger.info("成功获取用户等级: userId={}, LearningMode={}", userId, user.getLearningMode());
        return user != null ? user.getLearningMode() : null;
    }

    @Override
    public AppearanceMode getAppearanceMode(Integer userId) {
        if (userId == null) {
            logger.warn("获取外观模式失败: 用户ID为空");
            return null;
        }
        User user = this.getById(userId);
        logger.info("成功获取用户等级: userId={}, AppearanceMode={}", userId, user.getAppearanceMode());
        return user != null ? user.getAppearanceMode() : null;
    }




    @Override
    public boolean syncUserStatistics(Integer userId) {
        return false;
    }
}
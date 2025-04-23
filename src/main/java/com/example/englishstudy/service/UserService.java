package com.example.englishstudy.service;

import com.example.englishstudy.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.englishstudy.enums.AppearanceMode;
import com.example.englishstudy.enums.LearningMode;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;


public interface UserService extends IService<User> {
    /**
     * 用户注册（通过手机号验证码）
     * 注：如果用户绑定手机号需要发送验证短信，可以通过 UserVerificationCodesService.sendVerificationCode(String phoneNumber) 实现
     * @param phone 手机号
     * @param verificationCode 验证码
     * @return 注册成功返回 true，否则返回 false
     */
    User registerUserByPhone(String phone, String verificationCode);

    /**
     * 用户注册（通过微信登录）
     * @param code 前端传来的微信登录凭证 code
     * @return 注册成功返回 true，否则返回 false
     */
    User registerUserByWeChat(String code, String nickName);


    /**
     * 根据用户 ID 获取用户信息
     * @param userId 用户 ID
     * @return 用户对象，如果用户不存在则返回 null
     */
    User getUserById(Integer userId);


    /**已完成
     * 设置用户头像
     * @param userId 用户 ID
     * @param avatar 头像的 URL
     * @return 设置成功返回 true，否则返回 false
     */
    boolean setUserAvatar(Integer userId, String avatar);


    /**
     * 上传头像文件并返回访问URL
     * @param file 上传的文件
     * @return 文件访问路径
     * @throws IOException 文件操作异常
     */
    String uploadAvatar(MultipartFile file) throws IOException;



    /**已完成
     * 修改用户名
     * @param userId 用户 ID
     * @param newUserName 新的用户名
     * @return 修改成功返回 true，否则返回 false
     */
    boolean setUserName(Integer userId, String newUserName);


    /**
     * 设置用户学习词典
     * @param userId 用户 ID
     * @param bookId 词典 ID
     * @return 设置成功返回 true，否则返回 false
     */
    boolean setUserStudyBookId(Integer userId, String bookId);




    /**已完成
     * 更新用户信息（用户名、头像、性别、生日）
     * 设置用户个人信息
     * @param userId 用户 ID
     * @param age 用户年龄
     * @param gender 用户性别
     * @param phone 用户手机号
     * @param birthday 用户生日
     * @return 更新成功返回 true，否则返回 false
     */
    boolean updateUserInformation(Integer userId, Integer age, String gender, String phone, LocalDate birthday);

    /**已完成
     * 设置每日学习和复习量
     * 注：此方法应检查每日学习量和复习量是否超过设定范围
     * @param userId 用户 ID
     * @param dailyStudyAmount 每日学习量
     * @return 设置成功返回 true，否则返回 false
     */
    boolean updateUserLearningSettings(Integer userId, Integer dailyStudyAmount);


    int getUserLearningSettings(Integer userId);


    /**已完成
     * 设置学习模式（中英/英中）
     * @param userId 用户 ID
     * @param learningMode 学习模式，使用 LearningMode 枚举类型
     * @return 设置成功返回 true，否则返回 false
     */
    boolean updateLearningMode(Integer userId, LearningMode learningMode);

    /**已完成
     * 设置外观模式（白天/黑夜）
     * @param userId 用户 ID
     * @param appearanceMode 外观模式，使用 AppearanceMode 枚举类型
     * @return 设置成功返回 true，否则返回 false
     */
    boolean updateAppearanceMode(Integer userId, AppearanceMode appearanceMode);



    /**
     * 根据用户 ID 获取用户的经验值
     * @param userId 用户 ID
     * @return 用户的经验值，如果用户不存在则返回 null
     */
    Integer getUserExperience(Integer userId);

    /**
     * 根据用户 ID 获取用户等级
     * @param userId 用户 ID
     * @return 用户等级
     */
    int getUserLevel(Integer userId);



    /**
     * 获取用户头像URL
     * @param userId 用户ID
     * @return 头像URL，如果用户不存在则返回null
     */
    String getUserAvatar(Integer userId);

    /**
     * 获取用户名
     * @param userId 用户ID
     * @return 用户名，如果用户不存在则返回null
     */
    String getUserName(Integer userId);

    /**
     * 获取用户学习模式
     * @param userId 用户ID
     * @return 学习模式，如果用户不存在则返回null
     */
    LearningMode getLearningMode(Integer userId);

    /**
     * 获取用户外观模式
     * @param userId 用户ID
     * @return 外观模式，如果用户不存在则返回null
     */
    AppearanceMode getAppearanceMode(Integer userId);


    /**
     * 同步用户数据统计
     * 注：此方法应定期调用，同步用户的统计信息
     * @param userId 用户 ID
     * @return 同步成功返回 true，否则返回 false
     */
    boolean syncUserStatistics(Integer userId);
}

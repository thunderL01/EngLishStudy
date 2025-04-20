package com.example.englishstudy.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.englishstudy.entity.User;
import com.example.englishstudy.entity.UserCheckins;
import com.example.englishstudy.entity.UserWordStudyInterval;
import com.example.englishstudy.mapper.UserCheckinsMapper;
import com.example.englishstudy.service.UserCheckinsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.service.UserService;
import com.example.englishstudy.utils.SpringContextUtil;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserCheckinsServiceImpl extends ServiceImpl<UserCheckinsMapper, UserCheckins> 
    implements UserCheckinsService {


    private static final int DEFAULT_EXPERIENCE = 10; // 每次签到获得的默认经验值
    private static final Logger logger = LoggerFactory.getLogger(UserCheckinsServiceImpl.class);

    private final UserService userService;

    public UserCheckinsServiceImpl(UserService userService) {
        this.userService = userService;
    }


    //具体逻辑为：
    //（1）检查用户今天是否已经签到，如果已经签到则返回 false。
    //（2）获取用户上一次的签到记录，判断是否为连续签到。
    //（3）根据连续签到情况更新连续签到天数和总签到天数。
    //（4）保存新的签到记录，如果保存成功则返回 true，否则返回 false。
    //（5）签到后，还会更新用户的经验值。
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean checkin(Integer userId) {
        LocalDate today = LocalDate.now();
        try {
            // 检查用户今天是否已经签到
            Optional<UserCheckins> todayCheckin = this.lambdaQuery()
                    .eq(UserCheckins::getUserId, userId)
                    .eq(UserCheckins::getCheckinDate, today)
                    .oneOpt();

            if (todayCheckin.isPresent()) {
                logger.info("用户 ID {} 今天已经签到，无法重复签到", userId);
                return false; // 今天已经签到，返回 false
            }

            // 获取用户上一次签到记录
            Optional<UserCheckins> lastCheckin = this.lambdaQuery()
                    .eq(UserCheckins::getUserId, userId)
                    .orderByDesc(UserCheckins::getCheckinDate)
                    .last("LIMIT 1")
                    .oneOpt();

            UserCheckins newCheckin = new UserCheckins();
            newCheckin.setUserId(userId);
            newCheckin.setCheckinDate(today);
            newCheckin.setExperienceEarned(DEFAULT_EXPERIENCE);


            if (lastCheckin.isPresent()) {
                UserCheckins last = lastCheckin.get();
                if (last.getCheckinDate().plusDays(1).isEqual(today)) {
                    // 连续签到
                    newCheckin.setConsecutiveCheckinDays(last.getConsecutiveCheckinDays() + 1);
                    logger.info("用户 ID {} 连续签到，当前连续签到天数：{}", userId, newCheckin.getConsecutiveCheckinDays());
                } else {
                    // 非连续签到，连续签到天数重置为 1
                    newCheckin.setConsecutiveCheckinDays(1);
                    logger.info("用户 ID {} 非连续签到，连续签到天数重置为 1", userId);
                }
                newCheckin.setTotalCheckinDays(last.getTotalCheckinDays() + 1);
                // 赋值上一次签到日期
                newCheckin.setLastCheckinDate(last.getCheckinDate());
            } else {
                // 首次签到
                newCheckin.setConsecutiveCheckinDays(1);
                newCheckin.setTotalCheckinDays(1);
                logger.info("用户 ID {} 首次签到", userId);
            }

            // 保存签到记录
            boolean result = this.save(newCheckin);
            if (result) {
                logger.info("用户 ID {} 签到成功", userId);

                // 更新用户经验值
                LambdaQueryWrapper<User> countQuery = new LambdaQueryWrapper<>();
                countQuery.eq(User::getUserId, userId);

                User user =  userService.getOne(countQuery);

                // 检查用户是否存在
                if (user != null) {

                    int newExperience = user.getExperience() + DEFAULT_EXPERIENCE;
                    UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("user_id", userId);
                    updateWrapper.set("experience", newExperience);
                    boolean updateResult = userService.update(updateWrapper);

                    if (updateResult) {
                        logger.info("用户 ID {} 的经验值更新成功，当前经验值：{}", userId, newExperience);
                    } else {
                        logger.error("用户 ID {} 的经验值更新失败", userId);
                    }
                } else {
                    logger.error("未找到用户 ID 为 {} 的用户信息", userId);
                }
            } else {
                logger.error("用户 ID {} 签到记录保存失败", userId);
            }
            return result;
        } catch (Exception e) {
            logger.error("用户 ID {} 签到时发生异常", userId, e);
            return false;
        }
    }


    @Override
    public List<UserCheckins> getCheckinHistory(Integer userId, Integer days) {
        logger.info("开始查询用户 {} 的签到历史记录，查询天数：{}", userId, days);

        if (userId == null) {
            logger.warn("获取签到历史记录失败：用户ID不能为空");
            return Collections.emptyList();
        }

        int queryDays = (days == null || days <= 0) ? 30 : days;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(queryDays - 1);

        try {
            List<UserCheckins> checkinHistory = this.lambdaQuery()
                    .eq(UserCheckins::getUserId, userId)
                    .between(UserCheckins::getCheckinDate, startDate, endDate)
                    .orderByDesc(UserCheckins::getCheckinDate)
                    .list();
            logger.info("成功查询到用户 {} 的签到历史记录，记录数量：{}", userId, checkinHistory.size());
            return checkinHistory;
        } catch (Exception e) {
            logger.error("获取用户 {} 的签到历史记录时发生异常", userId, e);
            throw new RuntimeException("获取签到历史记录时发生异常", e);
        }
    }


    // 提取公共方法，用于查询用户的最后一条签到记录
    private Optional<UserCheckins> getLastCheckin(Integer userId) {
        try {
            return this.lambdaQuery()
                    .eq(UserCheckins::getUserId, userId)
                    .orderByDesc(UserCheckins::getCheckinDate)
                    .last("LIMIT 1")
                    .oneOpt();
        } catch (Exception e) {
            logger.error("查询用户 ID {} 的最后一条签到记录时发生异常", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public Integer getTotalCheckinDays(Integer userId) {
        logger.info("开始查询用户 ID {} 的累计签到天数", userId);
        // 调用提取的方法获取最后一条签到记录
        Optional<UserCheckins> lastCheckin = getLastCheckin(userId);

        if (lastCheckin.isPresent()) {
            Integer totalCheckinDays = lastCheckin.get().getTotalCheckinDays();
            logger.info("用户 ID {} 的累计签到天数为 {}", userId, totalCheckinDays);
            return totalCheckinDays;
        } else {
            logger.info("用户 ID {} 没有签到记录，累计签到天数为 0", userId);
            return 0;
        }
    }

    @Override
    public Integer getConsecutiveCheckinDays(Integer userId) {
        logger.info("开始查询用户 ID {} 的连续签到天数", userId);
        // 调用提取的方法获取最后一条签到记录
        Optional<UserCheckins> lastCheckin = getLastCheckin(userId);

        if (lastCheckin.isPresent()) {
            Integer consecutiveCheckinDays = lastCheckin.get().getConsecutiveCheckinDays();
            logger.info("用户 ID {} 的连续签到天数为 {}", userId, consecutiveCheckinDays);
            return consecutiveCheckinDays;
        } else {
            logger.info("用户 ID {} 没有签到记录，连续签到天数为 0", userId);
            return 0;
        }
    }


}



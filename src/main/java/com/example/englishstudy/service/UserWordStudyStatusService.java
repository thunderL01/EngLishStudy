package com.example.englishstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.englishstudy.entity.UserWordStudyStatus;
import com.example.englishstudy.entity.Word;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface UserWordStudyStatusService extends IService<UserWordStudyStatus> {




    /**
     * 记录在一天内单词学习过程的信息
     * @param userId 用户ID
     * @param wordId 单词ID
     * @param studyStatus 学习状态，如 "认识", "模糊", "忘记"
     * @return 保存结果
     */
    boolean recordLearning(Integer userId, Integer wordId, String studyStatus);


    /**
     * 统计指定用户需要学习的单词数量，最多返回 dailyStudyAmount 个
     * @param userId 用户 ID
     * @param dailyStudyAmount 每日学习数量上限
     * @return 需要学习的单词数量
     */
    int countWordsToLearn(Integer userId, int dailyStudyAmount);

    /**
     * 统计指定用户某天已完成的新学单词数量
     * @param userId 用户ID
     * @param studyDate 学习日期
     * @return 已完成的新学单词数量
     */
    int countCompletedNewStudyRecords(Integer userId, LocalDate studyDate);

    /**
     * 统计指定用户某天已完成的复习单词数量
     * @param userId 用户ID
     * @param studyDate 学习日期
     * @return 已完成的复习单词数量
     */
    int countCompletedReviewRecords(Integer userId, LocalDate studyDate);

    /**
     * 获取日期范围内的新学单词统计
     */
    Map<LocalDate, Integer> getNewStudyStats(Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取日期范围内的复习单词统计
     */
    Map<LocalDate, Integer> getReviewStats(Integer userId, LocalDate startDate, LocalDate endDate);


    /**
     * 根据学习规则获取需要学习的单词列表
     * 1. 优先级规则：忘记 > 模糊 > 记忆
     * 2. 考虑学习间隔：只返回remaining_interval <= 0的单词
     * 3. 排序规则：
     *    - 首先按学习状态优先级排序
     *    - 相同优先级的按学习日期排序
     * 4. 返回结果数量受dailyStudyAmount限制
     *
     * @param userId 用户ID，用于查询特定用户的学习记录
     * @return 符合条件且按优先级排序的单词列表
     */
    String getWordsToLearnByPriority(Integer userId);


    /**
     * 统计用户当天未完成学习的单词数量
     * @param userId 用户ID
     * @param dailyStudyAmount 每日学习量限制
     * @return 需要复习的单词数量
     */
    int countPendingReviewWordsByUserId(Integer userId, int dailyStudyAmount);

}
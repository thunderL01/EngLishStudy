package com.example.englishstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.englishstudy.entity.UserWordMemory;

import java.time.LocalDate;
import java.util.Map;

public interface UserWordMemoryService extends IService<UserWordMemory> {

    /**
     * 根据UserWordStudyStatus创建UserWordMemory记录,只记录userId，wordId，reviewCount（为0）
     * @param userId 用户ID
     * @param wordId 单词ID
     * @param studyDate 学习日期
     */
     boolean createMemoryRecord(Integer userId, Integer wordId, LocalDate studyDate);


    /**
     * 根据下次复习时间创建复习记录
     * @param userId 用户ID
     * @param wordId 单词ID
     * @return 是否创建成功
     */
    boolean createReviewRecord(Integer userId, Integer wordId);



    /**
     * 统计每天所有单词的平均记忆强度
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期到平均记忆强度的映射
     */
    Map<LocalDate, Double> getDailyAverageMemoryStrength(Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * 计算记忆强度
     * @param userId 用户ID
     * @param wordId 单词ID
     * @return 记忆强度值(0.0-1.0)
     */
    double calculateMemoryStrength(Integer userId, Integer wordId);




    /**
     * 计算下次复习时间
     * @param memory 用户单词记忆记录
     * @return 下次复习日期
     */
    LocalDate calculateNextReviewTime(UserWordMemory memory);



}
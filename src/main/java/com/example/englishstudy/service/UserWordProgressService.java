package com.example.englishstudy.service;

import com.example.englishstudy.entity.UserWordProgress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;


public interface UserWordProgressService extends IService<UserWordProgress> {


    /**
     * 记录用户学习单词的进度(存在问题，无法完成测试）
     *
     * @param userId 用户的唯一标识
     * @param wordId 单词的唯一标识
     * @param learnTime 学习单词的日期
     * @return 若记录成功返回 true，否则返回 false
     */
    boolean recordLearningProgress(Integer userId, Integer wordId,  LocalDate learnTime);


    /**
     * 获取用户对某个单词的学习历史记录(按学习时间倒序)
     */
    List<UserWordProgress> getProgressHistory(Integer userId, Integer wordId);

    /**
     * 获取用户对某个单词的学习次数
     */
    Integer getStudyCountByUserIdAndWordId(Integer userId, Integer wordId);

    /**
     * 获取用户最近一次学习某个单词的状态
     */
    String getLatestStudyStatus(Integer userId, Integer wordId);

    /**
     * 获取用户最近一次学习某个单词的日期
     */
    LocalDate getLatestStudyDate(Integer userId, Integer wordId);


}
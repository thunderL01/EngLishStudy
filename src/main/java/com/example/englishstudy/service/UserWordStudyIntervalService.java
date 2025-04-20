package com.example.englishstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.englishstudy.entity.UserWordStudyInterval;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface UserWordStudyIntervalService extends IService<UserWordStudyInterval> {

    /**
     * 记录用户单词的学习间隔
     *
     * @param userId 用户ID
     * @param wordId 单词ID
     * @param studyStatus 学习状态
     * @return 操作是否成功
     */
    boolean recordStudyInterval(Integer userId, Integer wordId, String studyStatus);

    /**
     * 减少用户所有单词的学习间隔剩余次数
     *
     * @param userId    用户ID
     * @return 操作是否成功
     */
    boolean decrementIntervals(Integer userId ,LocalDateTime studyDate);

}

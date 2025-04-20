package com.example.englishstudy.serviceimpl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.englishstudy.entity.UserWordProgress;
import com.example.englishstudy.entity.UserWordStudyInterval;
import com.example.englishstudy.entity.UserWordStudyStatus;
import com.example.englishstudy.mapper.UserWordProgressMapper;
import com.example.englishstudy.mapper.UserWordStudyStatusMapper;
import com.example.englishstudy.service.UserWordProgressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.service.UserWordStudyIntervalService;
import com.example.englishstudy.service.UserWordStudyStatusService;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


@Service
public class UserWordProgressServiceImpl extends ServiceImpl<UserWordProgressMapper, UserWordProgress>
    implements UserWordProgressService {


    private static final Logger logger = LoggerFactory.getLogger(UserWordProgressServiceImpl.class);


    private final UserWordStudyStatusMapper userWordStudyStatusMapper;
    private final UserWordStudyIntervalService userWordStudyIntervalService;

    public UserWordProgressServiceImpl(UserWordStudyStatusMapper userWordStudyStatusMapper, UserWordStudyIntervalService userWordStudyIntervalService) {
        this.userWordStudyStatusMapper = userWordStudyStatusMapper;
        this.userWordStudyIntervalService = userWordStudyIntervalService;
    }


    @Override
    public boolean recordLearningProgress(Integer userId, Integer wordId, LocalDate learnTime) {
        // 参数校验
        if (userId == null || wordId == null  || learnTime == null) {
            logger.error("记录用户学习进度时，传入的参数存在 null 值，userId: {}, wordId: {}, learnTime: {}", userId, wordId, learnTime);
            return false;
        }
        try {
            // 查询该用户对该单词的学习记录数量
            Integer studyCount = baseMapper.getStudyCountByUserIdAndWordId(userId, wordId);
            if (studyCount == null) {
                studyCount = 1;
            } else {
                studyCount++;
            }

            // 首先检查该单词是否已完成学习
            QueryWrapper<UserWordStudyStatus> statusQuery = new QueryWrapper<>();
            statusQuery.eq("user_id", userId)
                    .eq("word_id", wordId)
                    .eq("is_completed", true);
            boolean isCompleted = userWordStudyStatusMapper.selectCount(statusQuery) > 0;

            if (!isCompleted) {
                logger.info("单词学习未完成，无法记录进度，userId: {}, wordId: {}", userId, wordId);
                return false;
            }

            //其次查询该单词的学习间隔表的第一次学习记录，然后查询该记录的status
            QueryWrapper<UserWordStudyInterval> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("word_id", wordId)
                    .apply("DATE(study_date) = {0}", learnTime)
                    .eq("study_count", 1)
                    .last("LIMIT 1");

            UserWordStudyInterval firstStudyInterval = userWordStudyIntervalService.getOne(queryWrapper);
            if (firstStudyInterval == null) {
                logger.info("单词学习间隔表中没有找到第一次学习记录，无法记录进度，userId: {}, wordId: {}", userId, wordId);
                return false;
            }

            String firstStudyStatus = firstStudyInterval.getStudyStatus();
            logger.info("获取到第一次学习状态: {}, userId: {}, wordId: {}", firstStudyStatus, userId, wordId);

            // 创建新的学习记录
            UserWordProgress progress = new UserWordProgress();
            progress.setUserId(userId);
            progress.setWordId(wordId);
            progress.setStatus(firstStudyStatus);
            progress.setLearningTime(learnTime);
            progress.setStudyCount(studyCount);
            // 插入新记录
            boolean result = this.save(progress);
            logger.info("学习进度记录{}，userId: {}, wordId: {}", result ? "成功" : "失败", userId, wordId);
            return result;


        } catch (Exception e) {
            // 记录错误日志
            logger.error("记录用户学习进度时出现错误，userId: {}, wordId: {}", userId, wordId, e);
            return false;
        }
    }

    @Override
    public List<UserWordProgress> getProgressHistory(Integer userId, Integer wordId) {
        try {
            return lambdaQuery()
                    .eq(UserWordProgress::getUserId, userId)
                    .eq(UserWordProgress::getWordId, wordId)
                    .orderByDesc(UserWordProgress::getLearningTime)
                    .list();
        } catch (Exception e) {
            logger.error("获取用户学习历史记录失败，userId: {}, wordId: {}", userId, wordId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Integer getStudyCountByUserIdAndWordId(Integer userId, Integer wordId) {
        try {
            return baseMapper.getStudyCountByUserIdAndWordId(userId, wordId);
        } catch (Exception e) {
            logger.error("获取用户学习次数失败，userId: {}, wordId: {}", userId, wordId, e);
            return 0;
        }
    }

    @Override
    public String getLatestStudyStatus(Integer userId, Integer wordId) {
        try {
            UserWordProgress progress = lambdaQuery()
                    .eq(UserWordProgress::getUserId, userId)
                    .eq(UserWordProgress::getWordId, wordId)
                    .orderByDesc(UserWordProgress::getLearningTime)
                    .last("LIMIT 1")
                    .one();
            return progress != null ? progress.getStatus() : null;
        } catch (Exception e) {
            logger.error("获取最近学习状态失败，userId: {}, wordId: {}", userId, wordId, e);
            return null;
        }
    }

    @Override
    public LocalDate getLatestStudyDate(Integer userId, Integer wordId) {
        try {
            UserWordProgress progress = lambdaQuery()
                    .eq(UserWordProgress::getUserId, userId)
                    .eq(UserWordProgress::getWordId, wordId)
                    .orderByDesc(UserWordProgress::getLearningTime)
                    .last("LIMIT 1")
                    .one();
            return progress != null ? progress.getLearningTime() : null;
        } catch (Exception e) {
            logger.error("获取最近学习日期失败，userId: {}, wordId: {}", userId, wordId, e);
            return null;
        }
    }


}
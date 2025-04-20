package com.example.englishstudy.serviceimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.entity.UserWordStudyInterval;
import com.example.englishstudy.mapper.UserWordStudyIntervalMapper;
import com.example.englishstudy.service.UserWordStudyIntervalService;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class UserWordStudyIntervalServiceImpl extends ServiceImpl<UserWordStudyIntervalMapper, UserWordStudyInterval>
        implements UserWordStudyIntervalService {


    private static final Logger log = LoggerFactory.getLogger(UserWordStudyIntervalServiceImpl.class);


    @Override
    public boolean recordStudyInterval(Integer userId, Integer wordId, String studyStatus) {
        try {
            // 参数校验
            if (userId == null || wordId == null || studyStatus == null) {
                log.warn("参数不能为空: userId={}, wordId={}, studyStatus={}", userId, wordId, studyStatus);
                return false;
            }

            // 获取当前时间(每次调用都获取新时间)
            LocalDateTime now = LocalDateTime.now();


            // 计算间隔次数
            int remainingInterval = switch (studyStatus) {
                case "忘记" -> 2;
                case "模糊" -> 3;
                case "认识" -> 4;
                default -> 5;
            };

            // 创建新记录
            UserWordStudyInterval interval = new UserWordStudyInterval();
            interval.setUserId(userId);
            interval.setWordId(wordId);
            interval.setStudyStatus(studyStatus);
            interval.setRemainingInterval(remainingInterval);
            interval.setStudyDate(now);

            // 查询总学习次数(当天)
            LambdaQueryWrapper<UserWordStudyInterval> countQuery = new LambdaQueryWrapper<>();
            countQuery.eq(UserWordStudyInterval::getUserId, userId)
                    .eq(UserWordStudyInterval::getWordId, wordId)
                    .apply("DATE(study_date) = {0}", now.toLocalDate())
                    .orderByDesc(UserWordStudyInterval::getStudyDate)
                    .last("limit 1");

            UserWordStudyInterval lastRecord =  this.getOne(countQuery);
            log.info("lastRecord :{}",lastRecord );
            int MaxStudyCount = lastRecord != null ? lastRecord.getStudyCount() + 1 : 1;
            interval.setStudyCount(MaxStudyCount);

            boolean isUpdate = this.save(interval);
            if (isUpdate) {
                log.info("成功记录学习间隔: userId={}, wordId={}, status={}", userId, wordId, studyStatus);
                return true;
            }else {
                log.info("记录学习间隔失败: userId={}, wordId={}, status={}", userId, wordId, studyStatus);
                return false;
            }

        } catch (Exception e) {
            log.error("记录学习间隔异常: userId={}, wordId={}, status={}", userId, wordId, studyStatus, e);
            return false;
        }
    }

    @Override
    public boolean decrementIntervals(Integer userId , LocalDateTime studyDate) {


        try {
            boolean isUpdated = baseMapper.decrementIntervals(userId, studyDate);
            if (isUpdated) {
                log.info("成功减少学习间隔 - 用户ID: {}, 学习日期: {}", userId, studyDate);
                return isUpdated;
            }else {
                log.info("减少学习间隔失败，可能记录不存在 - 用户ID: {},  学习日期: {}",
                        userId, studyDate);
                return false;
            }

        } catch (Exception e) {
            log.error("减少用户单词学习间隔时发生异常，用户ID: {}, 学习日期: {}", userId, studyDate, e);
            return false;
        }
    }
}

package com.example.englishstudy.serviceimpl;

import com.example.englishstudy.entity.UserStudyTime;
import com.example.englishstudy.mapper.UserStudyTimeMapper;
import com.example.englishstudy.service.UserStudyTimeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
public class UserStudyTimeServiceImpl extends ServiceImpl<UserStudyTimeMapper, UserStudyTime>
        implements UserStudyTimeService {

    private static final Logger logger = LoggerFactory.getLogger(UserStudyTimeServiceImpl.class);

    @Override
    public Integer getTotalStudyTimeByUserId(Integer userId) {
        logger.info("开始查询用户 ID {} 的累计学习时长", userId);
        int result =  this.baseMapper.getTotalStudyTimeByUserId(userId);

        if (result != 0) {
            logger.info("用户 ID {} 的累计学习时长为 {}", userId, result);
            return this.baseMapper.getTotalStudyTimeByUserId(userId);
        }else{
            logger.info("用户 ID {} 没有累计学习时长", userId);
            return 0;
        }
    }

    @Override
    public boolean addStudyTimeRecord(Integer userId, Integer studyTime) {
        UserStudyTime record = new UserStudyTime();
        record.setUserId(userId);
        record.setUserStudyTimeDate(LocalDate.now());
        record.setStudyTime(studyTime);
        return this.save(record);
    }

    @Override
    public boolean updateStudyTimeRecord(Integer userId, Integer studyTime) {
        UserStudyTime record = new UserStudyTime();
        record.setUserId(userId);
        record.setUserStudyTimeDate(LocalDate.now());
        record.setStudyTime(studyTime);
        return this.updateById(record);
    }


}
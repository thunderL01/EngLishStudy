package com.example.englishstudy.service;

import com.example.englishstudy.entity.UserStudyTime;
import com.baomidou.mybatisplus.extension.service.IService;



public interface UserStudyTimeService extends IService<UserStudyTime> {

    /**
     * 根据用户ID获取总学习时间
     * @param userId 用户ID
     * @return 总学习时间（秒）
     */
    Integer getTotalStudyTimeByUserId(Integer userId);

    /**
     * 添加学习时间记录
     *
     * @return 是否添加成功
     */
    boolean addStudyTimeRecord(Integer userId, Integer studyTime);

    /**
     * 更新学习时间记录
     *
     * @return 是否更新成功
     */
    boolean updateStudyTimeRecord(Integer userId, Integer studyTime);





}
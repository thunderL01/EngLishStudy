package com.example.englishstudy.service;

import com.example.englishstudy.entity.UserCheckins;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface UserCheckinsService extends IService<UserCheckins> {

    /**已完成
     * 用户签到方法
     * @param userId 用户 ID
     * @return 签到成功返回 true，当天已签到或签到失败返回 false
     */
    boolean checkin(Integer userId);


    /**
     * 获取用户签到历史记录
     * @param userId 用户ID
     * @param days 查询天数（可选，默认30天）
     * @return 签到记录列表
     */
    List<UserCheckins> getCheckinHistory(Integer userId, Integer days);

    /**已完成
     * 获取用户的累计签到天数
     * @param userId 用户 ID
     * @return 累计签到天数
     */
    Integer getTotalCheckinDays(Integer userId);

    /**
     * 获取用户的连续签到天数
     * @param userId 用户 ID
     * @return 连续签到天数
     */
    Integer getConsecutiveCheckinDays(Integer userId);




}
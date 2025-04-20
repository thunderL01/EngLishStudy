package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.UserStudyTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserStudyTimeMapper extends BaseMapper<UserStudyTime> {
    // 目前无自定义方法

    /**
     * 根据用户 ID 查询总学习时间
     * @param userId 用户 ID
     * @return 总学习时间
     */
    @Select("SELECT SUM(study_time) FROM user_study_time WHERE user_id = #{userId}")
    Integer getTotalStudyTimeByUserId(Integer userId);

}
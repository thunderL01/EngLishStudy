package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.UserWordStudyInterval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface UserWordStudyIntervalMapper extends BaseMapper<UserWordStudyInterval> {

    /**
     * 减少用户单词学习间隔的剩余次数
     * @param userId 用户ID
     * @param studyDateTime 学习时间(精确到时分秒)
     * @return 是否更新成功
     */
    @Update("UPDATE user_word_study_interval " +
            "SET remaining_interval = remaining_interval - 1 " +
            "WHERE user_id = #{userId} " +
            "AND DATE(study_date) = DATE(#{studyDateTime}) " + // 两边都使用DATE()函数确保类型匹配
            "AND remaining_interval > 0")
    boolean decrementIntervals(@Param("userId") Integer userId,
                               @Param("studyDateTime") LocalDateTime studyDateTime);



}

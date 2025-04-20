package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.UserWordMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface UserWordMemoryMapper extends BaseMapper<UserWordMemory> {


    @Select("SELECT COUNT(*) FROM user_word_study_status " +
            "WHERE user_id = #{userId} AND word_id = #{wordId} " +
            "AND is_completed = true " +
            "AND user_word_study_status_date != #{excludeDate}")
    Integer countReviewsExcludingDate(@Param("userId") Integer userId,
                                      @Param("wordId") Integer wordId,
                                      @Param("excludeDate") LocalDate excludeDate);

}
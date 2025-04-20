package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.UserWordProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserWordProgressMapper extends BaseMapper<UserWordProgress> {


    /**
     * 根据用户 ID 和单词 ID 查询学习记录数量
     * @param userId 用户 ID
     * @param wordId 单词 ID
     * @return 学习记录数量
     */
    @Select("SELECT COUNT(*) FROM user_word_progress " +
            "WHERE user_id = #{userId} AND word_id = #{wordId}")
    Integer getStudyCountByUserIdAndWordId(@Param("userId") Integer userId,
                                           @Param("wordId") Integer wordId);


}
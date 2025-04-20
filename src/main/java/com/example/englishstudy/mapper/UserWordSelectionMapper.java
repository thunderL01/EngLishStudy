package com.example.englishstudy.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.UserWordSelection;
import com.example.englishstudy.entity.Word;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserWordSelectionMapper extends BaseMapper<UserWordSelection> {


    @Select("SELECT word FROM user_word_selection uws " +
                   "JOIN word w ON uws.word_id = w.word_id " +
                   "WHERE uws.user_id = #{userId} AND uws.status = 1 " +
                   "AND w.book_id LIKE CONCAT(#{bookIdPrefix}, '%')")
    List<String> getSelectedWordsByBookIdPrefix(@Param("userId") Integer userId,
                                              @Param("bookIdPrefix") String bookIdPrefix);






}
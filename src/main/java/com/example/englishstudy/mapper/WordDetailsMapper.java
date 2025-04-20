package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.WordDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WordDetailsMapper extends BaseMapper<WordDetails> {


    /**
     * 通过单词 id 获取该表的除表主键 id 和单词 id 外的数据
     * @param wordId 单词 id
     * @return 该表的除表主键 id 和单词 id 外的数据列表
     */
    @Select("SELECT pos, definition FROM word_details WHERE word_id = #{wordId}")
    List<WordDetails> getWordDetailsByWordId(@Param("wordId") Integer wordId);

    /**
     * 通过单词获取该表的除表主键 id 和单词 id 外的数据
     * @param word 单词
     * @return 该表的除表主键 id 和单词 id 外的数据列表
     */
    @Select("SELECT pos, definition " +
            "FROM word_details " +
            "WHERE word_id = (SELECT word_id FROM word WHERE word = #{word})")
    List<WordDetails> getWordDetailsByWord(@Param("word") String word);

}
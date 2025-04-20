package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.WordRelWords;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WordRelWordsMapper extends BaseMapper<WordRelWords> {
    // 目前无自定义方法

    /**
     * 通过单词 id 获取该表的除表主键 id 和单词 id 外的数据
     * @param wordId 单词 id
     * @return 该表的除表主键 id 和单词 id 外的数据列表
     */
    @Select("SELECT rel_word, pos, rel_word_trans FROM word_rel_words WHERE word_id = #{wordId}")
    List<WordRelWords> getWordRelWordsByWordId(@Param("wordId") Integer wordId);

    /**
     * 通过单词获取该表的除表主键 id 和单词 id 外的数据
     * @param word 单词
     * @return 该表的除表主键 id 和单词 id 外的数据列表
     */
    @Select("SELECT  rel_word, pos, rel_word_trans " +
            "FROM word_rel_words " +
            "WHERE word_id = (SELECT word_id FROM word WHERE word = #{word})")
    List<WordRelWords> getWordRelWordsByWord(@Param("word") String word);


}
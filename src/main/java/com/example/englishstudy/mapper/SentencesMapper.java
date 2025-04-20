package com.example.englishstudy.mapper;


import com.example.englishstudy.entity.Sentences;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SentencesMapper extends BaseMapper<Sentences> {

    /**
     * 通过单词 id 获取单词例句的除例句 id 和单词 id 外的数据
     * @param wordId 单词 id
     * @return 单词例句的除例句 id 和单词 id 外的数据列表
     */
    @Select("SELECT sentence, sentence_trans FROM sentences WHERE word_id = #{wordId}")
    List<Sentences> getSentencesByWordId(@Param("wordId") Integer wordId);

    /**
     * 通过单词获取单词例句的除例句 id 和单词 id 外的数据
     * @param word 单词
     * @return 单词例句的除例句 id 和单词 id 外的数据列表
     */
    @Select("SELECT sentence, sentence_trans " +
            "FROM sentences " +
            "WHERE word_id = (SELECT word_id FROM word WHERE word = #{word})")
    List<Sentences> getSentencesByWord(@Param("word") String word);


}

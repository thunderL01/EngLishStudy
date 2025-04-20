package com.example.englishstudy.mapper;

import com.example.englishstudy.entity.Phrases;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PhrasesMapper extends BaseMapper<Phrases> {

    /**
     * 通过单词 id 获取单词搭配的除单词 id 外的数据
     * @param wordId 单词 id
     * @return 单词搭配的除单词 id 外的数据列表
     */
    @Select("SELECT phrase_content, phrase_trans FROM phrases WHERE word_id = #{wordId}")
    List<Phrases> getPhrasesByWordId(@Param("wordId") Integer wordId);

    /**
     * 通过单词获取单词搭配的除 phrase_id 和单词 id 外的数据
     * @param word 单词
     * @return 单词搭配的除 phrase_id 和单词 id 外的数据列表
     */
    @Select("SELECT phrase_content, phrase_trans " +
            "FROM phrases " +
            "WHERE word_id = (SELECT word_id FROM word WHERE word = #{word})")
    List<Phrases> getPhrasesByWord(@Param("word") String word);



}

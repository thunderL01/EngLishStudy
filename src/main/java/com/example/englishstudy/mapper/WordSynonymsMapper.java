package com.example.englishstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.englishstudy.entity.WordSynonyms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WordSynonymsMapper extends BaseMapper<WordSynonyms> {
    // 目前无自定义方法


    /**
     * 通过单词 id 获取该表的除表主键 id 和单词 id 外的数据
     * @param wordId 单词 id
     * @return 该表的除表主键 id 和单词 id 外的数据列表
     */
    @Select("SELECT  synonym_word, pos, synonym_word_trans FROM word_synonyms WHERE word_id = #{wordId}")
    List<WordSynonyms> getWordSynonymsByWordId(@Param("wordId") Integer wordId);

    /**
     * 通过单词获取该表的除表主键 id 和单词 id 外的数据
     * @param word 单词
     * @return 该表的除表主键 id 和单词 id 外的数据列表
     */
    @Select("SELECT synonym_word, pos, synonym_word_trans " +
            "FROM word_synonyms " +
            "WHERE word_id = (SELECT word_id FROM word WHERE word = #{word})")
    List<WordSynonyms> getWordSynonymsByWord(@Param("word") String word);


}
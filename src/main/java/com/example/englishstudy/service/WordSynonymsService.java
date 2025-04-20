package com.example.englishstudy.service;

import com.example.englishstudy.entity.WordSynonyms;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface WordSynonymsService extends IService<WordSynonyms> {


    /**
     * 通过单词 id 获取该表的除表主键 id 和单词 id 外的数据
     * @param wordId 单词 id
     * @return 该表的除表主键 id 和单词 id 外的数据列表
     */
    List<WordSynonyms> getWordSynonymsByWordId(Integer wordId);

    /**
     * 通过单词获取该表的除表主键 id 和单词 id 外的数据
     * @param word 单词
     * @return 该表的除表主键 id 和单词 id 外的数据列表
     */
    List<WordSynonyms> getWordSynonymsByWord(String word);


} 
package com.example.englishstudy.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.englishstudy.entity.Sentences;

import java.util.List;

public interface SentencesService extends IService<Sentences> {

    /**
     * 通过单词 id 获取单词例句的除例句 id 和单词 id 外的数据
     * @param wordId 单词 id
     * @return 单词例句的除例句 id 和单词 id 外的数据列表
     */
    List<Sentences> getSentencesByWordId(Integer wordId);

    /**
     * 通过单词获取单词例句的除例句 id 和单词 id 外的数据
     * @param word 单词
     * @return 单词例句的除例句 id 和单词 id 外的数据列表
     */
    List<Sentences> getSentencesByWord(String word);

}

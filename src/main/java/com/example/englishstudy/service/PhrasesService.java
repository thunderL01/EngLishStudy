package com.example.englishstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.englishstudy.entity.Phrases;

import java.util.List;

public interface PhrasesService extends IService<Phrases> {

    /**
     * 通过单词 id 获取单词搭配的除单词 id 外的数据
     * @param wordId 单词 id
     * @return 单词搭配的除单词 id 外的数据列表
     */
    List<Phrases> getPhrasesByWordId(Integer wordId);

    /**
     * 通过单词获取单词搭配的除 phrase_id 和单词 id 外的数据
     * @param word 单词
     * @return 单词搭配的除 phrase_id 和单词 id 外的数据列表
     */
    List<Phrases> getPhrasesByWord(String word);





}

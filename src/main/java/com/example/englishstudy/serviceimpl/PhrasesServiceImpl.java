package com.example.englishstudy.serviceimpl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.entity.Phrases;
import com.example.englishstudy.mapper.PhrasesMapper;
import com.example.englishstudy.service.PhrasesService;
import org.springframework.stereotype.Service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PhrasesServiceImpl extends ServiceImpl<PhrasesMapper, Phrases> implements PhrasesService {


    private static final Logger logger = LoggerFactory.getLogger(PhrasesServiceImpl.class);

    @Override
    public List<Phrases> getPhrasesByWordId(Integer wordId) {
        try {
            logger.info("尝试获取单词ID {} 的短语", wordId);
            List<Phrases> phrases = this.baseMapper.getPhrasesByWordId(wordId);
            logger.info("获取短语成功: wordId={}, 返回数据: {}", wordId, phrases);
            return phrases;
        } catch (Exception e) {
            logger.error("根据单词ID获取短语失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<Phrases> getPhrasesByWord(String word) {
        try {
            logger.info("尝试获取单词 {} 的短语", word);
            List<Phrases> phrases = this.baseMapper.getPhrasesByWord(word);
            logger.info("获取短语成功: word={}, 返回数据: {}", word, phrases);
            return phrases;
        } catch (Exception e) {
            logger.error("根据单词获取短语失败, word: {}", word, e);
            return null;
        }
    }
}

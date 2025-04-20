package com.example.englishstudy.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.entity.Sentences;
import com.example.englishstudy.mapper.SentencesMapper;
import com.example.englishstudy.service.SentencesService;
import org.springframework.stereotype.Service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SentencesServiceImpl extends ServiceImpl<SentencesMapper, Sentences> implements SentencesService {

    private static final Logger logger = LoggerFactory.getLogger(SentencesServiceImpl.class);

    @Override
    public List<Sentences> getSentencesByWordId(Integer wordId) {
        try {
            logger.info("尝试获取单词ID {} 的例句", wordId);
            List<Sentences> sentences = this.baseMapper.getSentencesByWordId(wordId);
            logger.info("获取例句成功: wordId={}, 返回数据: {}", wordId, sentences);
            return sentences;
        } catch (Exception e) {
            logger.error("根据单词ID获取例句失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<Sentences> getSentencesByWord(String word) {
        try {
            logger.info("尝试获取单词 {} 的例句", word);
            List<Sentences> sentences = this.baseMapper.getSentencesByWord(word);
            logger.info("获取例句成功: word={}, 返回数据: {}", word, sentences);
            return sentences;
        } catch (Exception e) {
            logger.error("根据单词获取例句失败, word: {}", word, e);
            return null;
        }
    }
}

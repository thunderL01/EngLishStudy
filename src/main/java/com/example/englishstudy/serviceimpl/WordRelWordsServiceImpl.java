package com.example.englishstudy.serviceimpl;


import com.example.englishstudy.entity.WordRelWords;
import com.example.englishstudy.mapper.WordRelWordsMapper;
import com.example.englishstudy.service.WordRelWordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WordRelWordsServiceImpl extends ServiceImpl<WordRelWordsMapper, WordRelWords> implements WordRelWordsService {

    private static final Logger logger = LoggerFactory.getLogger(WordRelWordsServiceImpl.class);

    @Override
    public List<WordRelWords> getWordRelWordsByWordId(Integer wordId) {
        logger.info("尝试获取单词ID {} 的同根词", wordId);
        try {
            List<WordRelWords> relWords = this.baseMapper.getWordRelWordsByWordId(wordId);
            logger.info("获取同根词成功: wordId={}, 返回数据: {}", wordId, relWords);
            return relWords;
        } catch (Exception e) {
            logger.error("根据单词ID获取同根词失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<WordRelWords> getWordRelWordsByWord(String word) {
        logger.info("尝试获取单词 {} 的同根词", word);
        try {
            List<WordRelWords> relWords = this.baseMapper.getWordRelWordsByWord(word);
            logger.info("获取同根词成功: word={}, 返回数据: {}", word, relWords);
            return relWords;
        } catch (Exception e) {
            logger.error("根据单词获取同根词失败, word: {}", word, e);
            return null;
        }
    }

}
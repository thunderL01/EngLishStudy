package com.example.englishstudy.serviceimpl;


import com.example.englishstudy.entity.WordSynonyms;
import com.example.englishstudy.mapper.WordSynonymsMapper;
import com.example.englishstudy.service.WordSynonymsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WordSynonymsServiceImpl extends ServiceImpl<WordSynonymsMapper, WordSynonyms> implements WordSynonymsService {


    private static final Logger logger = LoggerFactory.getLogger(WordSynonymsServiceImpl.class);

    @Override
    public List<WordSynonyms> getWordSynonymsByWordId(Integer wordId) {
        logger.info("尝试获取单词ID {} 的同义词", wordId);
        try {
            List<WordSynonyms> synonyms = this.baseMapper.getWordSynonymsByWordId(wordId);
            logger.info("获取同义词成功: wordId={}, 返回数据: {}", wordId, synonyms);
            return synonyms;
        } catch (Exception e) {
            logger.error("根据单词ID获取同义词失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<WordSynonyms> getWordSynonymsByWord(String word) {
        logger.info("尝试获取单词 {} 的同义词", word);
        try {
            List<WordSynonyms> synonyms = this.baseMapper.getWordSynonymsByWord(word);
            logger.info("获取同义词成功: word={}, 返回数据: {}", word, synonyms);
            return synonyms;
        } catch (Exception e) {
            logger.error("根据单词获取同义词失败, word: {}", word, e);
            return null;
        }
    }

}
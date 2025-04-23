package com.example.englishstudy.serviceimpl;

import com.example.englishstudy.entity.WordSynonyms;
import com.example.englishstudy.mapper.WordSynonymsMapper;
import com.example.englishstudy.service.WordSynonymsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WordSynonymsServiceImpl extends ServiceImpl<WordSynonymsMapper, WordSynonyms> implements WordSynonymsService {

    private static final Logger logger = LoggerFactory.getLogger(WordSynonymsServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<WordSynonyms> getWordSynonymsByWordId(Integer wordId) {
        String key = "wordSynonyms:wordId:" + wordId;
        List<WordSynonyms> synonyms = (List<WordSynonyms>) redisTemplate.opsForValue().get(key);
        if (synonyms != null) {
            logger.info("从 Redis 缓存中获取单词ID {} 的同义词", wordId);
            return synonyms;
        }
        try {
            logger.info("尝试获取单词ID {} 的同义词", wordId);
            synonyms = this.baseMapper.getWordSynonymsByWordId(wordId);
            if (synonyms != null) {
                redisTemplate.opsForValue().set(key, synonyms, 30, TimeUnit.MINUTES);
                logger.info("将单词ID {} 的同义词存入 Redis 缓存，过期时间为30分钟", wordId);
            }
            logger.info("获取同义词成功: wordId={}, 返回数据: {}", wordId, synonyms);
            return synonyms;
        } catch (Exception e) {
            logger.error("根据单词ID获取同义词失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<WordSynonyms> getWordSynonymsByWord(String word) {
        String key = "wordSynonyms:word:" + word;
        List<WordSynonyms> synonyms = (List<WordSynonyms>) redisTemplate.opsForValue().get(key);
        if (synonyms != null) {
            logger.info("从 Redis 缓存中获取单词 {} 的同义词", word);
            return synonyms;
        }
        try {
            logger.info("尝试获取单词 {} 的同义词", word);
            synonyms = this.baseMapper.getWordSynonymsByWord(word);
            if (synonyms != null) {
                redisTemplate.opsForValue().set(key, synonyms, 30, TimeUnit.MINUTES);
                logger.info("将单词 {} 的同义词存入 Redis 缓存，过期时间为30分钟", word);
            }
            logger.info("获取同义词成功: word={}, 返回数据: {}", word, synonyms);
            return synonyms;
        } catch (Exception e) {
            logger.error("根据单词获取同义词失败, word: {}", word, e);
            return null;
        }
    }
}
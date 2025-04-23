package com.example.englishstudy.serviceimpl;

import com.example.englishstudy.entity.WordRelWords;
import com.example.englishstudy.mapper.WordRelWordsMapper;
import com.example.englishstudy.service.WordRelWordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WordRelWordsServiceImpl extends ServiceImpl<WordRelWordsMapper, WordRelWords> implements WordRelWordsService {

    private static final Logger logger = LoggerFactory.getLogger(WordRelWordsServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    @Override
    public List<WordRelWords> getWordRelWordsByWordId(Integer wordId) {
        String key = "wordRelWords:wordId:" + wordId;
        List<WordRelWords> relWords = (List<WordRelWords>) redisTemplate.opsForValue().get(key);
        if (relWords != null) {
            logger.info("从 Redis 缓存中获取单词ID {} 的同根词", wordId);
            return relWords;
        }
        try {
            logger.info("尝试获取单词ID {} 的同根词", wordId);
            relWords = this.baseMapper.getWordRelWordsByWordId(wordId);
            if (relWords != null) {
                redisTemplate.opsForValue().set(key, relWords, 30, TimeUnit.MINUTES);
                logger.info("将单词ID {} 的同根词存入 Redis 缓存，过期时间为30分钟", wordId);
            }
            logger.info("获取同根词成功: wordId={}, 返回数据: {}", wordId, relWords);
            return relWords;
        } catch (Exception e) {
            logger.error("根据单词ID获取同根词失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<WordRelWords> getWordRelWordsByWord(String word) {
        String key = "wordRelWords:word:" + word;
        List<WordRelWords> relWords = (List<WordRelWords>) redisTemplate.opsForValue().get(key);
        if (relWords != null) {
            logger.info("从 Redis 缓存中获取单词 {} 的同根词", word);
            return relWords;
        }
        try {
            logger.info("尝试获取单词 {} 的同根词", word);
            relWords = this.baseMapper.getWordRelWordsByWord(word);
            if (relWords != null) {
                redisTemplate.opsForValue().set(key, relWords, 30, TimeUnit.MINUTES);
                logger.info("将单词 {} 的同根词存入 Redis 缓存，过期时间为30分钟", word);
            }
            logger.info("获取同根词成功: word={}, 返回数据: {}", word, relWords);
            return relWords;
        } catch (Exception e) {
            logger.error("根据单词获取同根词失败, word: {}", word, e);
            return null;
        }
    }

}
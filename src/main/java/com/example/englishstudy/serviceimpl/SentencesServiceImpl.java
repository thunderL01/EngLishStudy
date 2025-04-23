package com.example.englishstudy.serviceimpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.englishstudy.entity.Sentences;
import com.example.englishstudy.mapper.SentencesMapper;
import com.example.englishstudy.service.SentencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SentencesServiceImpl extends ServiceImpl<SentencesMapper, Sentences> implements SentencesService {

    private static final Logger logger = LoggerFactory.getLogger(SentencesServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Sentences> getSentencesByWordId(Integer wordId) {
        String key = "sentences:wordId:" + wordId;
        List<Sentences> sentences = (List<Sentences>) redisTemplate.opsForValue().get(key);
        if (sentences != null) {
            logger.info("从 Redis 缓存中获取单词ID {} 的例句", wordId);
            return sentences;
        }
        try {
            logger.info("尝试获取单词ID {} 的例句", wordId);
            sentences = this.baseMapper.getSentencesByWordId(wordId);
            if (sentences != null) {
                redisTemplate.opsForValue().set(key, sentences, 30, TimeUnit.MINUTES);
                logger.info("将单词ID {} 的例句存入 Redis 缓存，过期时间为30分钟", wordId);
            }
            logger.info("获取例句成功: wordId={}, 返回数据: {}", wordId, sentences);
            return sentences;
        } catch (Exception e) {
            logger.error("根据单词ID获取例句失败, wordId: {}", wordId, e);
            return null;
        }
    }

    @Override
    public List<Sentences> getSentencesByWord(String word) {
        String key = "sentences:word:" + word;
        List<Sentences> sentences = (List<Sentences>) redisTemplate.opsForValue().get(key);
        if (sentences != null) {
            logger.info("从 Redis 缓存中获取单词 {} 的例句", word);
            return sentences;
        }
        try {
            logger.info("尝试获取单词 {} 的例句", word);
            sentences = this.baseMapper.getSentencesByWord(word);
            if (sentences != null) {
                redisTemplate.opsForValue().set(key, sentences, 30, TimeUnit.MINUTES);
                logger.info("将单词 {} 的例句存入 Redis 缓存，过期时间为30分钟", word);
            }
            logger.info("获取例句成功: word={}, 返回数据: {}", word, sentences);
            return sentences;
        } catch (Exception e) {
            logger.error("根据单词获取例句失败, word: {}", word, e);
            return null;
        }
    }
}